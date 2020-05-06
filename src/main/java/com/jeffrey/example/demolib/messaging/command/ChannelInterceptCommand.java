package com.jeffrey.example.demolib.messaging.command;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@FunctionalInterface
public interface ChannelInterceptCommand<R> {

    R invoke(Message<?> message, MessageChannel channel) throws Exception;

}
