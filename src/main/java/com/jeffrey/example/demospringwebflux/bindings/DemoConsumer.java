package com.jeffrey.example.demospringwebflux.bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class DemoConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoConsumer.class);

    /**
     * Reactive Consumer with a Function<Flux<?>, Mono<Void>> return type, instructing
     * SCSt framework with no reference to subscribe to (no output), invoking then
     * operator as the last operator on the stream.
     */
    @Bean
    public Function<Flux<Message<?>>, Mono<Void>> consumerRx0() {
        return flux -> flux.map(_message -> {
            LOGGER.debug("rx0 - receiving: {}", _message.toString());
            return _message;
        }).then(); //not redirecting to any output stream
    }

    @Bean
    public Function<Flux<String>, Mono<Void>> consumerRx1() {
        return flux -> flux.doOnNext(value -> {
            LOGGER.debug("rx1 - receiving: {}", value);
        }).then();
    }

    @Bean Function<Flux<String>, Flux<String>> consumerRx2() {
        return flux -> flux.doOnNext(value -> {
            LOGGER.debug("rx2 - receiving: {}", value);
        });
    }

    @Bean
    public Consumer<String> consumer0() {
        return value -> { LOGGER.debug("consumer0 - receiving: {}", value); };
    }

    /**
     * Composing Function with Consumer will result in Consumer.
     * See:
     * https://cloud.spring.io/spring-cloud-function/reference/html/spring-cloud-function.html#_composing_non_functions
     */
    @Bean
    public Function<String, String> consumer1() {
        return value -> {
            LOGGER.debug("consumer1: {}", value);
            return value;
        };
    }

}
