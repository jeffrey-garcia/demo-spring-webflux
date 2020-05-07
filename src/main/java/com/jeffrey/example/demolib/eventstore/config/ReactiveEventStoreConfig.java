package com.jeffrey.example.demolib.eventstore.config;

import com.jeffrey.example.demolib.eventstore.aop.advice.ConsumerAdviceInvocator;
import com.jeffrey.example.demolib.eventstore.aop.advice.SupplierAdviceInvocator;
import com.jeffrey.example.demolib.eventstore.service.EventStoreService;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Configuration class which hook up reactive event store components with externalized configuration.
 *
 * @author Jeffrey Garcia Wong
 */
@Configuration
public class ReactiveEventStoreConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveEventStoreConfig.class);

    @Autowired
    ApplicationContext applicationContext;

    @Bean("consumerInterceptor")
    public Advice consumerInterceptor(
        @Autowired @Qualifier("EventStoreService") EventStoreService eventStoreService
    ) {
        return new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                if (!methodInvocation.getMethod().getName().equals("accept")) {
                    return methodInvocation.proceed();
                }

                ReflectiveMethodInvocation reflectiveMethodInvocation = ((ReflectiveMethodInvocation) methodInvocation);
                LOGGER.debug("intercept consumer - join point signature: {}", reflectiveMethodInvocation.toString());
                LOGGER.debug("intercept consumer - intercepted method declared in class: {}", reflectiveMethodInvocation.getMethod().getDeclaringClass().getTypeName());
                LOGGER.debug("intercept consumer - proxy class: {}", reflectiveMethodInvocation.getProxy().getClass().getName());
                LOGGER.debug("intercept consumer - implementing class: {}", reflectiveMethodInvocation.getThis().getClass().getName());

                Object[] args = methodInvocation.getArguments();
                Assert.notNull(args, "arguments should not be null");
                Assert.isTrue(args.length==1, "Consumer's accept method should only have 1 parameter");

                if (args[0] instanceof Flux<?>) {
                    // IMPORTANT: accept() only entered once for Flux stream!!!
                    Flux<?> interceptedFlux = ConsumerAdviceInvocator.invokeReactive((Flux<?>)args[0], eventStoreService);
                    reflectiveMethodInvocation.setArguments(interceptedFlux);
                    return methodInvocation.proceed();

                } else {
                    Object interceptedObject = ConsumerAdviceInvocator.invoke(args[0]);
                    reflectiveMethodInvocation.setArguments(interceptedObject);
                    return methodInvocation.proceed();
                }
            }
        };
    }

    @Bean("supplierInterceptor")
    public Advice supplierInterceptor(
        @Autowired @Qualifier("EventStoreService") EventStoreService eventStoreService
    ) {
        return new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                if (!methodInvocation.getMethod().getName().equals("get")) {
                    return methodInvocation.proceed();
                }

                ReflectiveMethodInvocation reflectiveMethodInvocation = ((ReflectiveMethodInvocation) methodInvocation);
                LOGGER.debug("intercept supplier - join point signature: {}", reflectiveMethodInvocation.toString());
                LOGGER.debug("intercept supplier - intercepted method declared in class: {}", reflectiveMethodInvocation.getMethod().getDeclaringClass().getTypeName());
                LOGGER.debug("intercept supplier - proxy class: {}", reflectiveMethodInvocation.getProxy().getClass().getName());
                LOGGER.debug("intercept supplier - implementing class: {}", reflectiveMethodInvocation.getThis().getClass().getName());

                Object result = methodInvocation.proceed();
                if (result instanceof Flux<?>) {
                    // IMPORTANT: get() only entered once for Flux stream!!!
                    return SupplierAdviceInvocator.invokeReactive((Flux<?>) result, eventStoreService);
                } else {
                    return SupplierAdviceInvocator.invoke(result);
                }
            }
        };
    }

//    @Bean("supplierChannelInterceptor")
//    public Advice supplierChannelInterceptor() {
//        return new MethodInterceptor() {
//            @Override
//            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
//                if (!methodInvocation.getMethod().getName().equals("send")) {
//                    return methodInvocation.proceed();
//                }
//
//                ReflectiveMethodInvocation reflectiveMethodInvocation = ((ReflectiveMethodInvocation) methodInvocation);
//                LOGGER.debug("intercept supplier - join point signature: {}", reflectiveMethodInvocation.toString());
//                LOGGER.debug("intercept supplier - intercepted method declared in class: {}", reflectiveMethodInvocation.getMethod().getDeclaringClass().getTypeName());
//                LOGGER.debug("intercept supplier - proxy class: {}", reflectiveMethodInvocation.getProxy().getClass().getName());
//                LOGGER.debug("intercept supplier - implementing class: {}", reflectiveMethodInvocation.getThis().getClass().getName());
//
//                Object[] args = methodInvocation.getArguments();
//                Assert.notNull(args, "arguments should not be null");
//                Assert.isTrue(args.length>=1, "MessageChannel send method should have at least 1 parameter");
//                if (args.length == 1) {
//                    // TODO:
//                }
//
//                return methodInvocation.proceed();
//            }
//        };
//    }

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

    @Bean("supplierProxyCreator")
    public BeanNameAutoProxyCreator supplierProxyCreator() {
        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();

        /**
         * To intercept the message data, intercept the output channel instead of the Supplier,
         * Supplier's get() function doesn't have input parameter.
         *
         * However, Supplier's output channel will only be created right after all the beans are
         * instantiated and all dependencies are injected, therefore we can't populate the supplier
         * channels until application state becomes ready.
         *
         * Lookup through binding configurations is also not an option since we can't dictate
         * the bindings is an input or output channel until the channel is created.
         */
//        final String[] bindableBeanNames = applicationContext.getBeanNamesForType(Bindable.class);

        final String[] suppliers = applicationContext.getBeanNamesForType(Supplier.class);
        beanNameAutoProxyCreator.setBeanNames(suppliers); // supplier bean

        // Spring AOP is based around Around advice delivered via MethodInterceptor
//        beanNameAutoProxyCreator.setInterceptorNames("supplierChannelInterceptor");
        beanNameAutoProxyCreator.setInterceptorNames("supplierInterceptor");

        return beanNameAutoProxyCreator;
    }

}
