package com.jeffrey.example.demolib.eventstore.aop.advice;

import com.jeffrey.example.demolib.eventstore.service.EventStoreService;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class ConsumerAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAdviceInvocator.class);

    public static Flux<?> invokeReactive(Flux<?> inputFlux) {
        return inputFlux.doOnNext(value -> {
            LOGGER.debug("intercepting stream of data: {}", value);
        });
    }

    public static Object invokeReactive2(Flux<?> inputFlux, MethodInvocation proceedingJoinPoint, EventStoreService eventStoreService) {
        return inputFlux.map(value -> {
            try {
                if (value instanceof Message<?>) {
                    Message<?> message = (Message<?>) value;
                    String eventId = message.getHeaders().get("eventId", String.class);
                    String outputChannelName = message.getHeaders().get("outputChannelName", String.class);
                    Object result = proceedingJoinPoint.proceed();
                    eventStoreService.updateEventAsConsumed(eventId, outputChannelName);
                    return result;
                }
                return proceedingJoinPoint.proceed();

            } catch (Throwable throwable) {
                // wraps a checked exception into a special runtime exception that can be handled by onError
                throw Exceptions.propagate(throwable);
            }
        })
        .onErrorContinue((throwable, o) -> {
            LOGGER.error("error: {} encountered while processing: {}", throwable, o);
        });
    }

    public static <R> R invoke(R input) {
        LOGGER.debug("intercepting data: {}", input);
        return input;
    }

}
