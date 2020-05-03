package com.jeffrey.example.demolib.eventstore.aop.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class ConsumerAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAdviceInvocator.class);

    public static Flux<?> invokeReactive(Flux<?> inputFlux) {
        return inputFlux.doOnNext(value -> {
            LOGGER.debug("intercepting stream of data: {}", value);
        });
    }

    public static <R> R invoke(R input) {
        LOGGER.debug("intercepting data: {}", input);
        return input;
    }

}
