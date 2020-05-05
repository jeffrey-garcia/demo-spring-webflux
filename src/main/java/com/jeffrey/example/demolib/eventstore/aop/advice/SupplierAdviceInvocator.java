package com.jeffrey.example.demolib.eventstore.aop.advice;

import com.jeffrey.example.demolib.eventstore.publisher.EmitterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.util.UUID;

public class SupplierAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupplierAdviceInvocator.class);

    public static Flux<?> invokeReactive(Flux<?> outputFlux) {
        return outputFlux.map(value -> {
            try {
                Assert.notNull(value, "value must not be null");
                LOGGER.debug("intercepting stream of data: {}", value);

                if (value instanceof Message<?>) {
                    // apply any event-store pre-processing before sending to channel
//                    Message<?> message = ((Message<?>) value);
//                    Message<?> interceptedMessage = MessageBuilder.withPayload(message.getPayload())
//                            .copyHeaders(message.getHeaders())
//                            .setHeader("eventId", UUID.randomUUID().toString())
//                            .build();
//                    EmitterHandler.transform(message, interceptedMessage);
//                    return interceptedMessage;
                }

                return value;

            } catch (Throwable throwable) {
                // wraps a checked exception into a special runtime exception that can be handled by onError
                throw Exceptions.propagate(throwable);
            }
        })
        .onErrorContinue((throwable, value) -> {
            LOGGER.error("error encountered while intercepting supplier: {}", throwable.getMessage());
            LOGGER.error("message is discarded: {}", value);
        });
    }

    public static <R> R invoke(R output) {
        LOGGER.debug("intercepting data: {}", output);
        return output;
    }
}
