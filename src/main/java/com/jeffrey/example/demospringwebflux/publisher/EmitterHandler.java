package com.jeffrey.example.demospringwebflux.publisher;


import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;

import java.util.*;
import java.util.stream.Collectors;

public final class EmitterHandler<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmitterHandler.class);

    public static EmitterHandler getInstance() {
        return LazyLoader.instance;
    }

    private static class LazyLoader {
        private static final EmitterHandler instance = new EmitterHandler();
    }

    EmitterHandler() {};

//    private final Set<EmitterCallback<?>> emitterCallbacks = Sets.newConcurrentHashSet();
    private final Map<T, EmitterCallback<?>> emitterInputCallbacks = Maps.newConcurrentMap();
    private final Map<Message<T>, EmitterCallback<?>> emitterOutputCallbacks = Maps.newConcurrentMap();

    public static <T> EmitterCallback <T> emits(EmitterProcessor<EmitterCallback<T>> emitterProcessor, T data) {
        EmitterCallback<T> _callback = new EmitterCallback<>(data);
        emitterProcessor.onNext(_callback);
        return _callback;
    }

    public EmitterCallback<?> emitDataAndCreateCallback(EmitterProcessor<T> emitterProcessor, T sourceData) {
        EmitterCallback<?> _callback = new EmitterCallback<>(sourceData);
//        emitterCallbacks.add(_callback);
        emitterInputCallbacks.put(sourceData, _callback);
        emitterProcessor.onNext(sourceData);
        return _callback;
    }

    private Optional<EmitterCallback<?>> findCallbackWithInputData(T sourceData) {
        return Optional.ofNullable(emitterInputCallbacks.remove(sourceData));
//        return emitterCallbacks.stream().filter(callback -> {
//            return (callback.getInput()!=null && callback.getInput().equals((sourceData)));
//        }).findFirst();
    }

    public void transform(T sourceData, Message<T> transformedData) {
        findCallbackWithInputData(sourceData).ifPresent(callback -> {
            EmitterCallback<T> emitterCallback = ((EmitterCallback<T>)callback);
            LOGGER.debug("transform data: {}", transformedData);
//            emitterCallback.transform(transformedData);
            emitterOutputCallbacks.put(transformedData, emitterCallback);
        });
    }

    private Optional<EmitterCallback<?>> findCallbackWithTransformedData(Message<T> transformedData) {
        return Optional.ofNullable(emitterOutputCallbacks.remove(transformedData));
//        return emitterCallbacks.stream().filter(callback -> {
//            return (callback.getTransformed()!=null && callback.getTransformed().equals((transformedData)));
//        }).findFirst();
    }

    public void notifySuccess(Message<T> transformedData) {
        findCallbackWithTransformedData(transformedData).ifPresent(callback -> {
            EmitterCallback<T> emitterCallback = ((EmitterCallback<T>)callback);
            T sourceData = emitterCallback.getInput();
            LOGGER.debug("notify success: {}", sourceData);
            emitterCallback.output(sourceData);
//            emitterCallbacks.remove(emitterCallback);
        });
    }

    public void notifyFail(Message<T> transformedData, Throwable throwable) {
        findCallbackWithTransformedData(transformedData).ifPresent(callback -> {
            LOGGER.debug("notify fail: {}", throwable.getMessage());
            EmitterCallback<T> emitterCallback = ((EmitterCallback<T>)callback);
            emitterCallback.error(throwable);
//            emitterCallbacks.remove(emitterCallback);
        });
    }

}
