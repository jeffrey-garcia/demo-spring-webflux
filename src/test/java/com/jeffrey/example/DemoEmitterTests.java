package com.jeffrey.example;

import com.jeffrey.example.demoapp.entity.DemoEntity;
import com.jeffrey.example.demolib.eventstore.publisher.EmitterHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.UUID;

@RunWith(JUnit4.class)
public class DemoEmitterTests {

    @Test
    public void verifyMonoProcessor() {
        EmitterProcessor<DemoEntity> emitterProcessor = EmitterProcessor.create();

        emitterProcessor.doOnNext(demoEntity -> {
            Message<DemoEntity> message = MessageBuilder
                    .withPayload(demoEntity)
                    .setHeader("eventId", UUID.randomUUID().toString())
                    .build();
            EmitterHandler.transform(demoEntity, message);

            if (demoEntity.getData() == null)
                EmitterHandler.notifyFail(message, new RuntimeException("demo entity data is null"));
            else {
                EmitterHandler.notifySuccess(message);
            }
        }).subscribe();

        DemoEntity demoEntity = System.currentTimeMillis()%2 == 0 ? new DemoEntity("testing"):new DemoEntity(null);
        Mono<?> callback = EmitterHandler.create(demoEntity);

        // fire the data after the callback is subscribed
        emitterProcessor.onNext(demoEntity);

        Mono<ResponseEntity<DemoEntity>> responseEntityMono = callback.map(output -> {
            return ResponseEntity.ok((DemoEntity)output);
        }).onErrorReturn(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        );

        Disposable subscription = responseEntityMono.subscribe(responseEntity -> {
            if (demoEntity.getData()!=null) {
                Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            } else {
                Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
            }
        });

        // TODO: verify if the subscriber has been fired once

        subscription.dispose();
    }

    @Test
    public void verifyFluxSink() {
        EmitterProcessor<DemoEntity> emitterProcessor = EmitterProcessor.create();

        Mono<DemoEntity> mono = Mono.create((MonoSink<DemoEntity> sink) -> {
            emitterProcessor.map(demoEntity -> {
                if (demoEntity.getData() == null) sink.error(new RuntimeException("error"));
                sink.success(demoEntity);
                return demoEntity;
            }).subscribe();

            emitterProcessor.onNext(new DemoEntity(null));
//        emitterProcessor.onNext(new DemoEntity("testing"));
        });

        Disposable subscription = mono.map(value -> {
            return true;
        }).onErrorReturn(false).subscribe(
            value -> System.out.println("result: " + value)
        );

        subscription.dispose();
    }

}
