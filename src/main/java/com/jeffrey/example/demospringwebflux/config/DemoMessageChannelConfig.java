package com.jeffrey.example.demospringwebflux.config;

import com.jeffrey.example.demospringwebflux.aop.DemoAspect;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.function.context.catalog.BeanFactoryAwareFunctionRegistry;
import org.springframework.cloud.stream.binding.Bindable;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class DemoMessageChannelConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoMessageChannelConfig.class);

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    GenericApplicationContext genericApplicationContext;

    @Autowired
    BindingServiceProperties bindingServiceProperties;

    @Autowired
    DemoAspect demoAspect;

    private volatile boolean isInitialized = false;

    @EventListener(ApplicationReadyEvent.class)
    protected void postApplicationStartup() throws Exception {
        if (!isInitialized) { // prevents spring framework double-fire application ready event
            isInitialized = true;
        } else {
            return;
        }

        scanMessageChannels();
        scanReactiveMessageChannels();
        scanFunctions();
    }

    void scanMessageChannels() {
        String[] bindableBeanNames = applicationContext.getBeanNamesForType(Bindable.class);

        for (String bindableBeanName:bindableBeanNames) {
            Bindable bindable = applicationContext.getBean(bindableBeanName, Bindable.class);

            // scan all consumers
            for (String binding:bindable.getInputs()) {
                Object bindableBean = beanFactory.getBean(binding);
                if (bindableBean instanceof AbstractMessageChannel) {
                    AbstractMessageChannel abstractMessageChannel = (AbstractMessageChannel) bindableBean;
                    LOGGER.debug("intercept consumer's channel name: {}", abstractMessageChannel.getBeanName());

                    // add input interceptor
                    abstractMessageChannel.addInterceptor(0, new ChannelInterceptor() {
                        @Nullable
                        @Override
                        public Message<?> preSend(Message<?> message, MessageChannel channel) {
                            LOGGER.debug("input to: {}", abstractMessageChannel.getBeanName());
                            LOGGER.debug("message id: {}", message.getHeaders().getId());
                            return message;
                        }
                    });
                }
            }

            // scan all supplier
            for (String binding:bindable.getOutputs()) {
                Object bindableBean = beanFactory.getBean(binding);
                if (bindableBean instanceof AbstractMessageChannel) {
                    DirectWithAttributesChannel directWithAttributesChannel = (DirectWithAttributesChannel) bindableBean;
                    LOGGER.debug("intercept supplier's channel name: {}", directWithAttributesChannel.getBeanName());

                    directWithAttributesChannel.setAttribute("test-key","test-value");
                    directWithAttributesChannel.setFailover(false);

                    // add output interceptor
                    directWithAttributesChannel.addInterceptor(0, new ChannelInterceptor() {
                        @Nullable
                        @Override
                        public Message<?> preSend(Message<?> message, MessageChannel channel) {
                            LOGGER.debug("output to: {}", directWithAttributesChannel.getBeanName());
                            LOGGER.debug("message id: {}", message.getHeaders().getId());
                            return message;
                        }
                    });
                }
            }
        }
    }

    void scanReactiveMessageChannels() {
        String [] reactiveChannels = applicationContext.getBeanNamesForType(FluxMessageChannel.class);
        for (String reactiveChannel:reactiveChannels) {
            Object reactiveChannelBean = beanFactory.getBean(reactiveChannel);
            if (reactiveChannelBean instanceof FluxMessageChannel) {
                FluxMessageChannel fluxMessageChannel = (FluxMessageChannel) reactiveChannelBean;
                LOGGER.debug("intercept reactive channel name: {}", fluxMessageChannel.getBeanName());
            }
        }
    }

    void scanFunctions() {
        BeanFactoryAwareFunctionRegistry functionRegistry = (BeanFactoryAwareFunctionRegistry)applicationContext.getBean("functionCatalog");

        LOGGER.debug("suppliers: {}", functionRegistry.getNames(Supplier.class));
        int supplierTotal = functionRegistry.getNames(Supplier.class).size();

        LOGGER.debug("consumers: {}", functionRegistry.getNames(Consumer.class));
        int consumerTotal = functionRegistry.getNames(Consumer.class).size();

        LOGGER.debug("functions: {}", functionRegistry.getNames(Function.class));
        int functionTotal = functionRegistry.getNames(Function.class).size();

        LOGGER.debug("function registry total: {}", functionRegistry.size());
        LOGGER.debug("function count total: {}", (supplierTotal + consumerTotal + functionTotal));

        String [] suppliers = applicationContext.getBeanNamesForType(Supplier.class);
        for (String functionName:suppliers) {
            BeanFactoryAwareFunctionRegistry.FunctionInvocationWrapper invocationWrapper = functionRegistry.lookup(functionName);
            Type functionType = invocationWrapper.getFunctionType();
            boolean isSupplier = invocationWrapper.isSupplier();

            Object functionBean = beanFactory.getBean(functionName);
            LOGGER.debug("function class: {}", functionBean.getClass());
        }

        String [] consumers = applicationContext.getBeanNamesForType(Consumer.class);
        for (String consumer:consumers) {
            if (!consumer.equals("consumer0")) continue;

            Consumer consumerBean = (Consumer)beanFactory.getBean(consumer);

            // dynamically register the bean with AOP proxy
            // create a factory that can generate a proxy for the given target object
            AspectJProxyFactory factory = new AspectJProxyFactory(consumerBean);

            factory.addAdvice(new MethodInterceptor() {
                @Override
                public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                    LOGGER.warn("@@@ before intercept");
                    Object obj = methodInvocation.proceed();
                    LOGGER.warn("@@@ after intercept");
                    return obj;
                }
            });

            // add an aspect, the class must be an @AspectJ aspect
            // you can call this as many times as you need with different aspects
            factory.addAspect(demoAspect);

            // now get the proxy object...
            Consumer proxy = factory.getProxy();
//            proxy.accept("testing 123");
        }

    }

}
