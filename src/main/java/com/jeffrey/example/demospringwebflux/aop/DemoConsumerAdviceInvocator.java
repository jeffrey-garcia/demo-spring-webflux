package com.jeffrey.example.demospringwebflux.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

//public class DemoConsumerAdviceInvocator<R> {
public class DemoConsumerAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoConsumerAdviceInvocator.class);

    public static Flux<?> invokeReactive(Flux<?> inputFlux) {
        return inputFlux.doOnNext(value -> {
            LOGGER.debug("intercepting stream of data: {}", value);
        });
    }

//    @SuppressWarnings("unchecked")
//    public Flux<R> interceptStreams(Flux<R> inputFlux) {
//        return (Flux<R>) invokeReactive(inputFlux);
//    }

    public static <R> R invoke(R input) {
        LOGGER.debug("intercepting data: {}", input);
        return input;
    }

//    public R intercept(R input) {
//        return invoke(input);
//    }

}
