package com.jeffrey.example.demospringwebflux.publisher;

import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.util.function.Function;

public final class EmitterCallback<T> {
    private final T sourceData;
    private final MonoProcessor<T> monoProcessor;

    private Message<T> transformedData;

    EmitterCallback(T sourceData) {
        this.sourceData = sourceData;
        this.monoProcessor = MonoProcessor.create();
    }

    public T getInput() {
        return sourceData;
    }

//    public void transform(Message<T> transformedData) {
//        this.transformedData = transformedData;
//    }

//    public Message<T> getTransformed() {
//        return this.transformedData;
//    }

    public void output(T resultData) {
        monoProcessor.onNext(resultData);
    }

    public void error(Throwable throwable) {
        monoProcessor.onError(throwable);
    }

    public <R> Mono<R> map(Function<? super T, ? extends R> mapper) {
        return monoProcessor.map(mapper);
    }

//    public Mono<T> onErrorMap(Function<? super Throwable, ? extends Throwable> mapper) {
//        return monoProcessor.onErrorMap(mapper);
//    }
}
