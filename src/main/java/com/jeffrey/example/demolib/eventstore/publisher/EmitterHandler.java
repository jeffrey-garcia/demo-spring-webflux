package com.jeffrey.example.demolib.eventstore.publisher;


import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EmitterHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmitterHandler.class);

    /**
     * A builder of ConcurrentMap instances that can have keys or values automatically wrapped in weak references.
     * See also:
     * https://guava.dev/releases/21.0/api/docs/com/google/common/collect/MapMaker.html
     */
    private static final ConcurrentMap<? super Object, Callback<? super Object>> emitterCallbacks = new MapMaker()
            .concurrencyLevel(4)
            .weakKeys()
            .weakValues()
            .makeMap();

    public static <T> Mono<?> create(T sourceData) {
        LOGGER.debug("create: {}", sourceData);
        Callback<? super Object> callback = new Callback<>(sourceData);
        emitterCallbacks.put(sourceData, callback);
        return callback.monoProcessor;
    }

    private static <T> Optional<Callback<? super Object>> findCallbackWithInputData(T sourceData) {
        Optional<Callback<? super Object>> callback = Optional.ofNullable(emitterCallbacks.remove(sourceData));
        if (!callback.isPresent()) {
            LOGGER.debug("callback not found for source data: {}", sourceData);
        }
        return callback;
    }

    public static <T,R> void transform(T sourceData, R transformedData) {
        LOGGER.debug("transform: {}", transformedData);
        findCallbackWithInputData(sourceData).ifPresent(callback -> {
            LOGGER.debug("re-mapping callback with transformed data: {}", transformedData);
            emitterCallbacks.put(transformedData, callback);
        });
    }

    private static <R> Optional<Callback<? super Object>> findCallbackWithTransformedData(R transformedData) {
        // TODO: add error handling
        final Message<?> transformedMessage = (Message<?>) transformedData;
        final String transformedMessageEventId = (String) transformedMessage.getHeaders().get("eventId");

        List<Map.Entry<? super Object, Callback<? super Object>>> entries = emitterCallbacks.entrySet().stream().filter(callbackEntry -> {
            final Object key = callbackEntry.getKey();
            if (!(key instanceof Message<?>)) return false;
            final Message<?> message = (Message<?>) key;
            if (!(message.getHeaders().get("eventId") instanceof String)) return false;
            final String eventId = (String) message.getHeaders().get("eventId");
            return (eventId!=null && eventId.equals(transformedMessageEventId));
        }).collect(Collectors.toList());

        if (entries.size()<=0) {
            LOGGER.debug("callback not found for transformed data: {}", transformedData);
            return Optional.empty();
        } else {
            final Callback<? super Object> callback = entries.get(0).getValue();
            emitterCallbacks.remove(entries.get(0).getKey(), entries.get(0).getValue());
            return Optional.ofNullable(callback);
        }
    }

    public static <R> void notifySuccess(R transformedData) {
        findCallbackWithTransformedData(transformedData).ifPresent(callback -> {
            Object sourceData = callback.getInput();
            LOGGER.debug("notify success: {}", sourceData);
            callback.output(sourceData);
        });
    }

    public static <R> void notifyFail(R transformedData, Throwable throwable) {
        findCallbackWithTransformedData(transformedData).ifPresent(callback -> {
            LOGGER.error("notify fail: {}", throwable.getMessage());
            callback.error(throwable);
        });
    }

    public static final class Callback<E> {
        private final E sourceData;
        private final MonoProcessor<E> monoProcessor;

        Callback(E sourceData) {
            this.sourceData = sourceData;
            this.monoProcessor = MonoProcessor.create();
        }

        private E getInput() {
            return sourceData;
        }

        private void output(E resultData) {
            monoProcessor.onNext(resultData);
        }

        private void error(Throwable throwable) {
            monoProcessor.onError(throwable);
        }

        public <R> Mono<R> map(Function<? super E, ? extends R> mapper) {
            return monoProcessor.map(mapper);
        }
    }

}
