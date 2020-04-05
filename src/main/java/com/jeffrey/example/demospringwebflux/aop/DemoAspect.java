package com.jeffrey.example.demospringwebflux.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class DemoAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoAspect.class);

    @SuppressWarnings("unused")
    @Pointcut("this(org.springframework.messaging.MessageChannel)")
    public void anyTargetClassThatImplementsMessageChannel() {}

    @SuppressWarnings("unused")
    @Pointcut("!within(org.springframework.cloud.stream.binder.*)")
    public void anyTargetClassExcludingBinderPackage() {}

    @SuppressWarnings("unused")
    @Pointcut("target(org.springframework.cloud.stream.messaging.DirectWithAttributesChannel)")
    public void targetClassIsDirectWithAttributesChannel() {}

    @SuppressWarnings("unused")
    @Pointcut("execution(public * org.springframework.messaging.MessageChannel.send(*))")
    public void targetClassImplementsMessageChannelSendMethod() {}

//    @Before("anyTargetClassExcludingBinderPackage() && anyTargetClassThatImplementsMessageChannel()")
    @Before("targetClassIsDirectWithAttributesChannel() && targetClassImplementsMessageChannelSendMethod()")
    @SuppressWarnings("unused")
    public void interceptOutboundChannel(JoinPoint jp) {
        LOGGER.debug("before called - join point signature: {}", jp.getSignature());
        LOGGER.debug("before called - intercepted method overridden by: {}", jp.getSignature().getDeclaringType().getName());
        LOGGER.debug("before called - proxy class: {}", jp.getThis().getClass().getName());
        LOGGER.debug("before called - implementing class: {}", jp.getTarget().getClass().getName());
    }

}
