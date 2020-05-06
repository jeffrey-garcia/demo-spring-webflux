package com.jeffrey.example.demolib.eventstore.aop.advice;

import com.jeffrey.example.demolib.eventstore.publisher.EmitterHandler;
import com.jeffrey.example.demolib.eventstore.service.EventStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class ConsumerAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAdviceInvocator.class);

//    public static Flux<?> invokeReactive(Flux<?> inputFlux) {
//        return inputFlux.doOnNext(value -> {
//            LOGGER.debug("intercepting incoming data stream: {}", value);
//        });
//    }

    public static Flux<?> invokeReactive(Flux<?> inputFlux, EventStoreService eventStoreService) {
        return inputFlux.map(value -> {
            try {
                Assert.notNull(value, "value must not be null");
                LOGGER.debug("intercepting stream of data: {}", value);

                if (value instanceof Message<?>) {
                    Message<?> message = (Message<?>) value;
                    String eventId = message.getHeaders().get("eventId", String.class);
                    String outputChannelName = message.getHeaders().get("outputChannelName", String.class);

                    if (!StringUtils.isEmpty(eventId) && !StringUtils.isEmpty(outputChannelName)) {
                        if (eventStoreService.isIgnoreDuplicate() &&
                            eventStoreService.hasEventBeenConsumed(eventId, outputChannelName)
                        ) {
                            LOGGER.warn("event: {} has been consumed, skipping", eventId);
                            // skip the consumer if the event has been consumed
                            throw new RuntimeException(String.format("event: %s has been consumed, skipping", eventId));
                        }

                        // de-duplication is not required or message has not been seen before

                        // create a callback subscription and get notified when consumer finished consuming the message
                        Mono<?> callback = EmitterHandler.create(value);
                        callback.doOnSubscribe(_subscription -> {
                            // on subscribed handling here
                        })
                        .doFinally(output -> {
                            // wrap-up handling here
                        })
                        .timeout(
                            // define timeout to release the mono if the response can't be produced timely
                            Duration.ofMillis(5000)
                        )
                        .doOnSuccess(result -> {
                            LOGGER.debug("message consumed, eventId: {}", eventId);
                            eventStoreService.updateEventAsConsumed(eventId, outputChannelName);
                        }).doOnError(throwable -> {
                            // manage if any user/application level error causing the failure of message consumption
                            LOGGER.error("error encountered while consuming the message: {}", throwable.getMessage());
                        })
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();
                    }
                }

                return value;

            } catch (Throwable throwable) {
                // wraps a checked exception into a special runtime exception that can be handled by onError
                throw Exceptions.propagate(throwable);
            }
        })
        .onErrorContinue((throwable, value) -> {
            LOGGER.error("error encountered while intercepting consumer: {}", throwable.getMessage());
            LOGGER.error("message is discarded: {}", value);
        });
    }

    public static <R> R invoke(R input) {
        LOGGER.debug("intercepting data: {}", input);
        return input;
    }

}
