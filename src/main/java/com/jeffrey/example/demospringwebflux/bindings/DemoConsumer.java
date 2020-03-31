package com.jeffrey.example.demospringwebflux.bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class DemoConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoConsumer.class);

    /**
     * Reactive Consumer with a a Function<Flux<?>, Mono<Void>> return type, instructing
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

//    @Bean
//    public Consumer<String> even() {
//        return value -> System.out.println("EVEN: " + value);
//    }

//    @Bean
//    public Consumer<String> even() {
//        return value -> {
//            throw new RuntimeException("intentional");
//        };
//    }


//    @StreamListener("test-out-0")
//    public void receive(
//        DemoEntity demoEntity
//    ) throws Exception {
//        LOGGER.debug("message received: {}", demoEntity.toString());
//    }

}
