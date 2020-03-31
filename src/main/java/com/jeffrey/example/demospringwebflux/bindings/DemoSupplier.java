package com.jeffrey.example.demospringwebflux.bindings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

@Component
public class DemoSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoSupplier.class);

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    @Qualifier("demoEntityEmitProcessor")
    EmitterProcessor<DemoEntity> demoEntityEmitterProcessor;

    /**
     * Produces (supplies) the continuous stream of messages and not an individual message.
     * triggered only once instead of periodically
     */
    @Bean
    public Supplier<Flux<Message<DemoEntity>>> supplierRx0() {
        return () ->
                demoEntityEmitterProcessor.doOnNext(_demoEntity -> {
                    try {
                        String jsonString = jsonMapper.writeValueAsString(_demoEntity);
                        LOGGER.debug("rx0 - emitting entity: {}", jsonString);
                    } catch (JsonProcessingException e) {
                        //TODO: how to handle error in Flux
                        LOGGER.error(e.getMessage(), e);
                    }

                }).map(_demoEntity -> {
                    Message<DemoEntity> message = MessageBuilder.withPayload(_demoEntity).build();
                    LOGGER.debug("sending message - headers: {}", message.getHeaders().toString());
                    LOGGER.debug("sending message - payload: {}", message.getPayload().toString());
                    return message;
                });
    }

//    /**
//     * produces (supplies) the continuous stream of messages and not an individual message.
//     * triggered only once instead of periodically
//     */
//    @Bean
//    public Supplier<Flux<String>> rx1() {
//        return () -> Flux.fromStream(Stream.generate(new Supplier<String>() {
//            @Override
//            public String get() {
//                try {
//                    Thread.sleep(1000);
//                    LOGGER.debug("rx1 - Hello from Supplier");
//                    return "Hello from Supplier";
//                } catch (Exception e) {
//                    // ignore
//                    return null;
//                }
//            }
//        })).subscribeOn(Schedulers.elastic()).share();
//    }
//
//    /**
//     * poll some data source and return a finite stream of data representing the result
//     * given the finite nature of the produced stream, such Supplier still needs to be invoked periodically.
//     */
//    @PollableBean
//    public Supplier<Flux<String>> rx2() {
//        return () -> Flux.just("hello", "bye")
//                .doOnNext(value -> LOGGER.debug("rx2 - value: {}", value))
//                .subscribeOn(Schedulers.elastic()).share();
//    }
//
//    @Bean(name = "test")
//    public Supplier<DemoEntity> output() {
//        return () -> {
//            LOGGER.debug("rx3");
//            return new DemoEntity(null);
//        };
//    }

}
