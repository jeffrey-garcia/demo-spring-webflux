package com.jeffrey.example.demospringwebflux.aop;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.publisher.EmitterHandler;
import com.jeffrey.example.demospringwebflux.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.UUID;

public class DemoSupplierAdviceInvocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoSupplierAdviceInvocator.class);

    public static Flux<?> invokeReactive(Flux<?> outputFlux, DemoService demoService) {
        return outputFlux.map(value -> {
            Assert.notNull(value, "value must not be null");
            LOGGER.debug("intercepting stream of data: {}", value);

            if (value instanceof Message<?>) {
                // TODO: add error handling
                Message<?> message = ((Message<?>) value);
                Message<?> interceptedMessage = MessageBuilder.withPayload(message.getPayload())
                        .copyHeaders(message.getHeaders())
                        .setHeader("eventId", UUID.randomUUID().toString())
                        .build();
                EmitterHandler.transform(message, interceptedMessage);
                return interceptedMessage;
            }
            return value;

//            try {
//                DemoEntity demoEntity = ((Message<DemoEntity>) value).getPayload();
//                demoService.createDemoEntity(demoEntity);
//                EmitterHandler.notifySuccess(value);
//            } catch (Throwable throwable) {
//                EmitterHandler.notifyFail(value, throwable);
//                throw throwable;
//            }
//            return value;
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
