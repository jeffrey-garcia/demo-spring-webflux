package com.jeffrey.example.demolib.eventstore.aop.advice;

import com.jeffrey.example.demolib.eventstore.publisher.EmitterHandler;
import com.jeffrey.example.demolib.eventstore.service.EventStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class SupplierAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupplierAdviceInvocator.class);

//    public static Flux<?> invokeReactive(Flux<?> inputFlux) {
//        return inputFlux.doOnNext(value -> {
//            LOGGER.debug("intercepting outgoing data stream: {}", value);
//        });
//    }

    public static Flux<?> invokeReactive(Flux<?> outputFlux, EventStoreService eventStoreService) {
        return outputFlux.map(value -> {
            try {
                Assert.notNull(value, "value must not be null");

                if (value instanceof Message<?>) {
                    LOGGER.debug("intercepting supplier stream: {}", value);
                    // apply event-store pre-processing before sending to channel

                    // temporary workaround:
                    // generate an eventId as key to reference the emitter callback at supplier side
                    Message<?> message = ((Message<?>) value);
                    Message<?> interceptedMessage = MessageBuilder.withPayload(message.getPayload())
                            .copyHeaders(message.getHeaders())
                            .setHeader("eventId", eventStoreService.generateEventId())
                            .build();
                    EmitterHandler.transform(message, interceptedMessage);

                    return interceptedMessage;
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
