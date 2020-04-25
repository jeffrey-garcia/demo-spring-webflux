package com.jeffrey.example.demospringwebflux.aop;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.publisher.EmitterHandler;
import com.jeffrey.example.demospringwebflux.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

public class DemoSupplierAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoSupplierAdviceInvocator.class);

    public static Flux<?> invokeReactive(Flux<?> outputFlux, DemoService demoService) {
        return outputFlux.map(value -> {
            LOGGER.debug("intercepting stream of data: {}", value);
            try {
                Assert.notNull(value, "value must not be null");
                Assert.isTrue(value instanceof Message<?>, "value must be instance of message");
                Assert.isTrue(((Message<?>)value).getPayload() instanceof DemoEntity, "value must be instance of DemoEntity");

                DemoEntity demoEntity = ((Message<DemoEntity>) value).getPayload();
                demoService.createDemoEntity(demoEntity);
                EmitterHandler.notifySuccess(value);
            } catch (Throwable throwable) {
                EmitterHandler.notifyFail(value, throwable);
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
