package com.jeffrey.example.demospringwebflux.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.service.DemoRxService;
import com.jeffrey.example.demospringwebflux.service.DemoService;
import com.jeffrey.example.demospringwebflux.util.MonoResultWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;


/**
 * [IMPORTANT] this aspect should only be used for blocking channel
 *
 * TODO:
 * add handling to separate this aspect to intercept any non-blocking channel
 */
@Aspect
public class DemoAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoAspect.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ObjectMapper jsonMapper;

    @Autowired
    DemoService demoService;

    @Autowired
    DemoRxService demoRxService;


//    @Deprecated
//    @SuppressWarnings("unused")
//    @Pointcut("this(org.springframework.messaging.MessageChannel)")
//    public void anyTargetClassThatImplementsMessageChannel() {}

//    @Deprecated
//    @SuppressWarnings("unused")
//    @Pointcut("!within(org.springframework.cloud.stream.binder.*)")
//    public void anyTargetClassExcludingBinderPackage() {}

    @SuppressWarnings("unused")
    @Pointcut("target(org.springframework.cloud.stream.messaging.DirectWithAttributesChannel)")
    public void targetClassIsDirectWithAttributesChannel() {}

//    @Deprecated
//    @SuppressWarnings("unused")
//    @Pointcut("execution(public * org.springframework.messaging.MessageChannel.send(*))")
//    public void targetClassImplementsMessageChannelSendMethod() {}

    @SuppressWarnings("unused")
    @Pointcut("execution(public * org.springframework.messaging.MessageChannel.send(*)) && args(org.springframework.messaging.Message)")
    public void targetClassImplementsMessageChannelSendMethodWithArgumentMessage() {}

//    @Before("anyTargetClassExcludingBinderPackage() && anyTargetClassThatImplementsMessageChannel()") // this works, but not preferred as it's not intuitive and difficult to reason about
//    @Before("targetClassIsDirectWithAttributesChannel() && targetClassImplementsMessageChannelSendMethod()") // this is preferred but not the best
//    @SuppressWarnings("unused")
//    public void interceptBeforeOutboundChannel(JoinPoint joinPoint) {
//        LOGGER.debug("intercept supplier - join point signature: {}", joinPoint.getSignature());
//        LOGGER.debug("intercept supplier - intercepted method overridden by: {}", joinPoint.getSignature().getDeclaringType().getName());
//        LOGGER.debug("intercept supplier - proxy class: {}", joinPoint.getThis().getClass().getName());
//        LOGGER.debug("intercept supplier - implementing class: {}", joinPoint.getTarget().getClass().getName());
//    }

    @Around("targetClassIsDirectWithAttributesChannel() && targetClassImplementsMessageChannelSendMethodWithArgumentMessage()")
    @SuppressWarnings("unused")
    public Object interceptAroundOutboundChannel(
            ProceedingJoinPoint proceedingJoinPoint
    ) throws Throwable
    {
        LOGGER.debug("intercept supplier - join point signature: {}", proceedingJoinPoint.getSignature());
        LOGGER.debug("intercept supplier - intercepted method overridden by: {}", proceedingJoinPoint.getSignature().getDeclaringType().getName());
        LOGGER.debug("intercept supplier - proxy class: {}", proceedingJoinPoint.getThis().getClass().getName());
        LOGGER.debug("intercept supplier - implementing class: {}", proceedingJoinPoint.getTarget().getClass().getName());

        // safe casting - pointcut guaranteed the target class is instance of AbstractMessageChannel
        String channelName = ((AbstractMessageChannel) proceedingJoinPoint.getTarget()).getBeanName();
        LOGGER.debug("intercept supplier - channel name: {}", channelName);
        // TODO: verify of the channel is an output channel

        Object[] args = proceedingJoinPoint.getArgs();
        Assert.notNull(args, "argument must be provided for MessageChannel.send(Message<?> message)");
        Assert.isTrue(args.length==1, "there must be only one argument for MessageChannel.send(Message<?> message)");
        Assert.notNull(args[0], "argument value cannot be null for MessageChannel.send(Message<?> message)");
        Assert.isTrue(args[0] instanceof org.springframework.messaging.Message<?>, "argument must be of type org.springframework.messaging.Message<?>");
        Assert.notNull(((Message<?>)args[0]).getPayload(), "message payload cannot be null");
        Assert.isTrue(((Message<?>)args[0]).getPayload() instanceof byte[], "message payload should be byte[] array");

        // payload conversion to entity before saving to DB
        Message<?> message = (Message<?>) args[0];
        byte [] bytes = (byte[])message.getPayload();
        String jsonString = new String(bytes);
        DemoEntity demoEntity = this.jsonMapper.readValue(jsonString, DemoEntity.class);

        LOGGER.debug("saving entity to DB: {}", jsonString);

        // this will create a blocking behavior which defeats the rationale of reactive programming
        // thus facilitate guarantee of atomic behavior for write DB and send message to broker
//        demoService.createDemoEntity(demoEntity);

        // this is non-blocking but cannot propagate database write error back to controller
        MonoResultWrapper<Object> resultWrapper = new MonoResultWrapper<>();

        Mono<Boolean> resultMono = executeJoinPoint(writeEntity(demoEntity, resultWrapper), proceedingJoinPoint, resultWrapper);
        resultMono.subscribe();

        if (resultWrapper.getThrowable()!=null) {
            throw resultWrapper.getThrowable();
        }
        return resultWrapper.getResult();
    }

    private Mono<Boolean> writeEntity(
            DemoEntity demoEntity,
            MonoResultWrapper resultWrapper)
    {
        return Mono.just(demoEntity)
                .map(_demoEntity -> {
                    demoRxService.createDemoEntity(demoEntity).subscribe();
                    return _demoEntity;
                })
                .onErrorMap(throwable -> {
                    resultWrapper.setThrowable(throwable);
                    return throwable;
                })
                .map(value -> {
                    return true;
                })
                .onErrorReturn(
                    false
                )
                .onErrorStop();
    }

    private Mono<Boolean> executeJoinPoint(
            Mono<Boolean> mono,
            ProceedingJoinPoint proceedingJoinPoint,
            MonoResultWrapper resultWrapper)
    {
        return mono
                .map(writeDbResult -> {
                    LOGGER.debug("write DB result: {}", writeDbResult);
                    if (writeDbResult) {
                        try {
                            Object object = proceedingJoinPoint.proceed();
                            resultWrapper.setResult(object);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }
                    return writeDbResult;
                })
                .onErrorMap(throwable -> {
                    resultWrapper.setThrowable(throwable);
                    return throwable;
                })
                .map(value -> {
                    return value;
                })
                .onErrorReturn(
                    false
                )
                .onErrorStop();
    }

}
