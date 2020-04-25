package com.jeffrey.example.demospringwebflux.publisher;


import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class EmitterHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmitterHandler.class);

    private static final Map<? super Object, Callback<? super Object>> emitterInputCallbacks = Maps.newConcurrentMap();
    private static final Map<? super Object, Callback<? super Object>> emitterOutputCallbacks = Maps.newConcurrentMap();

    public static <T> Callback<?> create(EmitterProcessor<T> emitterProcessor, T sourceData) {
        Callback<? super Object> callback = new Callback<>(sourceData);
        emitterInputCallbacks.put(sourceData, callback);
        emitterProcessor.onNext(sourceData);
        return callback;
    }

    private static <T> Optional<Callback<? super Object>> findCallbackWithInputData(T sourceData) {
        return Optional.ofNullable(emitterInputCallbacks.remove(sourceData));
    }

    public static <T,R> void transform(T sourceData, R transformedData) {
        findCallbackWithInputData(sourceData).ifPresent(callback -> {
            LOGGER.debug("transform data: {}", transformedData);
            emitterOutputCallbacks.put(transformedData, callback);
        });
    }

    private static <R> Optional<Callback<? super Object>> findCallbackWithTransformedData(R transformedData) {
        return Optional.ofNullable(emitterOutputCallbacks.remove(transformedData));
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
