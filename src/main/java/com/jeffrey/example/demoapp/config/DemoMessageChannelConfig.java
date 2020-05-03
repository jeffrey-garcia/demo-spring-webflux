package com.jeffrey.example.demoapp.config;

import com.jeffrey.example.demolib.eventstore.aop.aspect.ReactiveEventStoreAspect;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.function.context.catalog.BeanFactoryAwareFunctionRegistry;
import org.springframework.cloud.stream.binding.Bindable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.channel.AbstractMessageChannel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    ConfigurableListableBeanFactory configurableListableBeanFactory;

//    @Autowired
//    ReactiveEventStoreAspect reactiveEventStoreAspect;

    private volatile boolean isInitialized = false;

    @EventListener(ApplicationReadyEvent.class)
    protected void postApplicationStartup() throws Exception {
        if (!isInitialized) { // prevents spring framework double-fire application ready event
            isInitialized = true;
        } else {
            return;
        }

//        scanSupplierMessageChannels();
//        scanConsumerFunctionsRegistry();
    }

    void scanSupplierMessageChannels() {
        final String[] bindableBeanNames = applicationContext.getBeanNamesForType(Bindable.class);
        final List<String> outputChannelList = new ArrayList<>();

        for (String bindableBeanName:bindableBeanNames) {
            Bindable bindable = applicationContext.getBean(bindableBeanName, Bindable.class);

            // scan all supplier
            for (String binding:bindable.getOutputs()) {
                Object bindableBean = beanFactory.getBean(binding);
                if (bindableBean instanceof AbstractMessageChannel) {
                    AbstractMessageChannel AbstractMessageChannel = (AbstractMessageChannel) bindableBean;
                    LOGGER.debug("supplier's channel name: {}", AbstractMessageChannel.getBeanName());
                    outputChannelList.add(AbstractMessageChannel.getBeanName());
                }
            }
        }

        if (outputChannelList.size()>0) {
            createSuppliersProxyByOutputChannels(outputChannelList);
        }
    }


    void createSuppliersProxyByOutputChannels(List<String> outputChannelList) {
        if (outputChannelList.size() <= 0) return;

        /**
         * To intercept the message data, intercept the output channel
         * instead of the supplier (get() function of supplier doesn't have input parameter)
         */
        String [] outputChannelNames = outputChannelList.toArray(new String[] {});
        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
        beanNameAutoProxyCreator.setBeanNames(outputChannelNames); // output channel bean
        Object supplierInterceptor = beanFactory.getBean("supplierInterceptor"); // throw Bean exception if not found

        // Spring AOP is based around Around advice delivered via MethodInterceptor
        beanNameAutoProxyCreator.setInterceptorNames("supplierInterceptor");

        configurableListableBeanFactory.registerSingleton("supplierProxyCreator", beanNameAutoProxyCreator);
    }

//    void scanConsumerFunctionsRegistry() {
//        BeanFactoryAwareFunctionRegistry functionRegistry = (BeanFactoryAwareFunctionRegistry)applicationContext.getBean("functionCatalog");
//
//        String [] suppliers = applicationContext.getBeanNamesForType(Supplier.class);
//        LOGGER.debug("supplier functions: {}", Arrays.asList(suppliers));
//
//        String [] processors = applicationContext.getBeanNamesForType(Function.class);
//        LOGGER.debug("processor functions: {}", Arrays.asList(processors));
//
//        String [] consumers = applicationContext.getBeanNamesForType(Consumer.class);
//        LOGGER.debug("consumer functions: {}", Arrays.asList(consumers));
//
//        LOGGER.debug("function registry total: {}", functionRegistry.size());
//        LOGGER.debug("functions total: {}", (suppliers.length + consumers.length + processors.length));
//
//        for (String supplier:suppliers) {
//            BeanFactoryAwareFunctionRegistry.FunctionInvocationWrapper invocationWrapper = functionRegistry.lookup(supplier);
//            Type functionType = invocationWrapper.getFunctionType();
//            boolean isSupplier = invocationWrapper.isSupplier();
//            boolean isConsumer = invocationWrapper.isConsumer();
//
//            // locate the supplier's function bean
//            Object functionBean = beanFactory.getBean(supplier);
//            LOGGER.debug("function class: {}", functionBean.getClass().getName());
//            LOGGER.debug("function type: {}", functionType);
//            LOGGER.debug("is supplier? {}", isSupplier);
//            LOGGER.debug("is consumer? {}", isConsumer);
//        }
//
//        for (String processor:processors) {
//            BeanFactoryAwareFunctionRegistry.FunctionInvocationWrapper invocationWrapper = functionRegistry.lookup(processor);
//            Type functionType = invocationWrapper.getFunctionType();
//            boolean isSupplier = invocationWrapper.isSupplier();
//            boolean isConsumer = invocationWrapper.isConsumer();
//
//            // a consumer can also be a processor in case of composed functions
//            // locate the processor's function bean
//            Object functionBean = beanFactory.getBean(processor);
//            LOGGER.debug("function class: {}", functionBean.getClass().getName());
//            LOGGER.debug("function type: {}", functionType);
//            LOGGER.debug("is processor? {}", !isSupplier && !isConsumer);
//        }
//
//        for (String consumer:consumers) {
//            BeanFactoryAwareFunctionRegistry.FunctionInvocationWrapper invocationWrapper = functionRegistry.lookup(consumer);
//            Type functionType = invocationWrapper.getFunctionType();
//            boolean isSupplier = invocationWrapper.isSupplier();
//            boolean isConsumer = invocationWrapper.isConsumer();
//
//            // locate the consumer's function bean
//            Object functionBean = beanFactory.getBean(consumer);
//            LOGGER.debug("function class: {}", functionBean.getClass().getName());
//            LOGGER.debug("function type: {}", functionType);
//            LOGGER.debug("is supplier? {}", isSupplier);
//            LOGGER.debug("is consumer? {}", isConsumer);
//
//            // dynamically register the bean with AOP proxy
//            // create a factory that can generate a proxy for the given target object
//            AspectJProxyFactory factory = new AspectJProxyFactory(functionBean);
//
//            factory.addAdvice(new MethodInterceptor() {
//                @Override
//                public Object invoke(MethodInvocation methodInvocation) throws Throwable {
//                    LOGGER.warn("@@@ before intercept");
//                    Object obj = methodInvocation.proceed();
//                    LOGGER.warn("@@@ after intercept");
//                    return obj;
//                }
//            });
//
//            // add an aspect, the class must be an @AspectJ aspect
//            // you can call this as many times as you need with different aspects
//            factory.addAspect(reactiveEventStoreAspect);
//
//            // now get the proxy object...
//            Consumer proxy = factory.getProxy();
////            proxy.accept("testing 123");
//        }
//
//    }

}
