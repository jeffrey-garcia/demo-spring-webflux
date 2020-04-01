package com.jeffrey.example.demospringwebflux.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.stream.binding.Bindable;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

@Configuration
public class DemoMessageChannelConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoMessageChannelConfig.class);

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    BindingServiceProperties bindingServiceProperties;

    private volatile boolean isInitialized = false;

    @EventListener(ApplicationReadyEvent.class)
    protected void postApplicationStartup() {
        if (!isInitialized) { // prevents spring framework double-fire application ready event
            isInitialized = true;
        } else {
            return;
        }

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
                    AbstractMessageChannel abstractMessageChannel = (AbstractMessageChannel) bindableBean;
                    LOGGER.debug("intercept supplier's channel name: {}", abstractMessageChannel.getBeanName());

                    // add output interceptor
                    abstractMessageChannel.addInterceptor(0, new ChannelInterceptor() {
                        @Nullable
                        @Override
                        public Message<?> preSend(Message<?> message, MessageChannel channel) {
                            LOGGER.debug("output to: {}", abstractMessageChannel.getBeanName());
                            LOGGER.debug("message id: {}", message.getHeaders().getId());
                            return message;
                        }
                    });
                }
            }
        }
    }
}
