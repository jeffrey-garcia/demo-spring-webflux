package com.jeffrey.example.demolib.eventstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import static com.jeffrey.example.demolib.eventstore.util.ChannelBindingAccessor.GLOBAL_ERROR_CHANNEL;
import static com.jeffrey.example.demolib.eventstore.util.ChannelBindingAccessor.GLOBAL_PUBLISHER_CONFIRM_CHANNEL;

/**
 * capture messages sent to the specified inputChannel
 */
@Configuration("ServiceActivatorConfig")
public class ServiceActivatorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceActivatorConfig.class);

    @ServiceActivator(inputChannel = GLOBAL_ERROR_CHANNEL)
    public void onError(Message<?> message) {
        LOGGER.debug("on error: {}", message);
    }

    @ServiceActivator(inputChannel = GLOBAL_PUBLISHER_CONFIRM_CHANNEL)
    public void onPublisherConfirm(Message<?> message) {
        LOGGER.debug("on publisher confirm: {}", message);
    }

}
