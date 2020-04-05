package com.jeffrey.example.demospringwebflux.bindings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.service.DemoRxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.function.context.PollableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
public class DemoSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoSupplier.class);

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private DemoRxService demoRxService;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    @Qualifier("demoEntityEmitProcessor")
    EmitterProcessor<DemoEntity> demoEntityEmitterProcessor;

    /**
     * Produces (supplies) the continuous stream of messages and not an individual message.
     * triggered only once instead of periodically
     */
    @Bean
    public Supplier<Flux<Message<DemoEntity>>> supplierRx0() {
        return () -> {
            return demoEntityEmitterProcessor.doOnNext(_demoEntity -> {
                LOGGER.debug("rx0 - emitting entity: {}", _demoEntity.toString());
            }).map(_demoEntity -> {
                Message<DemoEntity> message = MessageBuilder.withPayload(_demoEntity).build();
                LOGGER.debug("sending message - headers: {}", message.getHeaders().toString());
                LOGGER.debug("sending message - payload: {}", message.getPayload().toString());
                return message;
            });
        };
    }

    /**
     * Produces (supplies) a continuous stream of data (one message every 5 seconds)
     * Since the supplier function emits continuous stream of data, the poller would
     * trigger it only once
     */
    @Bean
    public Supplier<Flux<String>> supplierRx1() {
        return () -> Flux.fromStream(Stream.generate(() -> {
            try {
                Thread.sleep(5000);
                LOGGER.debug("rx1 - emitting: {}", "Hello from reactive Supplier");
                return "Hello from reactive Supplier";
            } catch (Exception e) {
                // ignore
                return null;
            }
        })).subscribeOn(Schedulers.elastic()).share();
    }

    /**
     * Produce a finite stream of data (consist of only 2 messages)
     * Due to the finite nature of the produced stream, this supplier function is required
     * to be invoked periodically by the poller.
     *
     * See spring.cloud.stream.poller.fixed-delay
     */
    @PollableBean
    public Supplier<Flux<String>> supplierRx2() {
        return () -> Flux.just("hello from reactive supplier", "bye reactive supplier")
                .doOnNext(value -> LOGGER.debug("rx2 - emitting: {}", value))
                .subscribeOn(Schedulers.elastic()).share();
    }

    /**
     * Produce a single message every second and each message is sent to a destination that
     * is exposed by the binder.
     */
    @Bean
    public Supplier<String> supplier0() {
        return () -> {
            String value = "Hello from supplier";
            LOGGER.debug("supplier0 - emitting: {}", value);
            return value;
        };
    }

}
