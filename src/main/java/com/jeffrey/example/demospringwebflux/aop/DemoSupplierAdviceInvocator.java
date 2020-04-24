package com.jeffrey.example.demospringwebflux.aop;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.publisher.EmitterHandler;
import com.jeffrey.example.demospringwebflux.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

public class DemoSupplierAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoSupplierAdviceInvocator.class);

    public static Flux<?> invokeReactive(Flux<?> outputFlux, DemoService demoService) {
//        return outputFlux.doOnNext(value -> {
//            LOGGER.debug("intercepting stream of data: {}", value);
//            throw new RuntimeException("error intercepting supplier's output");
//        });

        return outputFlux.map(value -> {
            LOGGER.debug("intercepting stream of data: {}", value);
//            throw new RuntimeException("error intercepting supplier's output");
//            return value;

            DemoEntity demoEntity = ((Message<DemoEntity>) value).getPayload();
            try {
                demoService.createDemoEntity(demoEntity);
                EmitterHandler.getInstance().notifySuccess((Message<?>)value);
            } catch (Throwable throwable) {
                EmitterHandler.getInstance().notifyFail((Message<?>)value, throwable);
                throw throwable;
            }

            return value;

        })
        .onErrorContinue((throwable, o) -> {
            LOGGER.error("error: {} encountered while processing: {}", throwable, o);
        });
    }

    public static <R> R invoke(R output) {
        LOGGER.debug("intercepting data: {}", output);
        return output;
    }
}
