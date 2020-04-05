package com.jeffrey.example.demospringwebflux.config;

import com.jeffrey.example.demospringwebflux.util.DemoConsumerAdviceInvocator;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Configuration
public class DemoProxyConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoProxyConfig.class);

    @Autowired
    ApplicationContext applicationContext;

    @Bean("consumerInterceptor")
    public Advice consumerInterceptor() {
        return new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                if (!methodInvocation.getMethod().getName().equals("accept")) {
                    return methodInvocation.proceed();
                }

                ReflectiveMethodInvocation reflectiveMethodInvocation = ((ReflectiveMethodInvocation) methodInvocation);
                LOGGER.debug("before intercept - join point signature: {}", reflectiveMethodInvocation.toString());
                LOGGER.debug("before intercept - intercepted method declared in class: {}", reflectiveMethodInvocation.getMethod().getDeclaringClass().getTypeName());
                LOGGER.debug("before intercept - proxy class: {}", reflectiveMethodInvocation.getProxy().getClass().getName());
                LOGGER.debug("before intercept - implementing class: {}", reflectiveMethodInvocation.getThis().getClass().getName());

                Object[] args = methodInvocation.getArguments();
                Assert.notNull(args, "arguments should not be null");
                Assert.isTrue(args.length==1, "Consumer's accept method should only have 1 parameter");

                if (args[0] instanceof Flux<?>) {
                    // IMPORTANT: accept() only entered once for Flux stream!!!
                    Flux<?> interceptedFlux = DemoConsumerAdviceInvocator.invokeReactive((Flux<?>)args[0]);
                    reflectiveMethodInvocation.setArguments(interceptedFlux);
                } else {
                    Object interceptedObject = DemoConsumerAdviceInvocator.invoke(args[0]);
                    reflectiveMethodInvocation.setArguments(interceptedObject);
                }

                return methodInvocation.proceed();
            }
        };
    }

    @Bean("supplierInterceptor")
    public Advice supplierInterceptor() {
        return new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                if (!methodInvocation.getMethod().getName().equals("send")) {
                    return methodInvocation.proceed();
                }

                ReflectiveMethodInvocation reflectiveMethodInvocation = ((ReflectiveMethodInvocation) methodInvocation);
                LOGGER.debug("before intercept - join point signature: {}", reflectiveMethodInvocation.toString());
                LOGGER.debug("before intercept - intercepted method declared in class: {}", reflectiveMethodInvocation.getMethod().getDeclaringClass().getTypeName());
                LOGGER.debug("before intercept - proxy class: {}", reflectiveMethodInvocation.getProxy().getClass().getName());
                LOGGER.debug("before intercept - implementing class: {}", reflectiveMethodInvocation.getThis().getClass().getName());

                Object[] args = methodInvocation.getArguments();
                Assert.notNull(args, "arguments should not be null");
                Assert.isTrue(args.length>=1, "MessageChannel send method should have at least 1 parameter");
                if (args.length == 1) {
                    // TODO:
                }

                return methodInvocation.proceed();
            }
        };
    }

    @Bean("consumerProxyCreator")
    public BeanNameAutoProxyCreator consumerProxyCreator() {
        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();

        // varargs function(Object... args) is the equivalent of a
        // method declared with an array function(Object[] args)
        //String [] consumers = new String[] {"consumerRx0", "consumer0"};
        String [] consumers = applicationContext.getBeanNamesForType(Consumer.class);

        // TODO: extract those consumers with remote bindings (input channel) and eligible for intercepting (is a consumer)

        beanNameAutoProxyCreator.setBeanNames(consumers); // consumer bean

        // Spring AOP is based around Around advice delivered via MethodInterceptor
        beanNameAutoProxyCreator.setInterceptorNames("consumerInterceptor");
        return beanNameAutoProxyCreator;
    }

//    @Bean("supplierProxyCreator")
//    public BeanNameAutoProxyCreator supplierProxyCreator() {
//        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
//
//        // TODO: extract those suppliers with remote bindings (output channel) and eligible for intercepting (is a supplier)
//
//        beanNameAutoProxyCreator.setBeanNames("supplier0-out-0"); // output channel bean
//
//        // Spring AOP is based around Around advice delivered via MethodInterceptor
//        beanNameAutoProxyCreator.setInterceptorNames("supplierInterceptor");
//        return beanNameAutoProxyCreator;
//    }

}
