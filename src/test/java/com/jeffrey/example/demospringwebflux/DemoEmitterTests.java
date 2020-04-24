package com.jeffrey.example.demospringwebflux;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.publisher.EmitterCallback;
import com.jeffrey.example.demospringwebflux.publisher.EmitterHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

@RunWith(JUnit4.class)
public class DemoEmitterTests {

    @Test
    public void verifyMonoProcessor() {
        EmitterProcessor<EmitterCallback<DemoEntity>> emitterProcessor = EmitterProcessor.create();

        emitterProcessor.doOnNext(callback -> {
            DemoEntity demoEntity = callback.getInput();
            if (demoEntity.getData() == null)
                throw new RuntimeException("error");
            else
                callback.output(demoEntity);
        }).onErrorContinue(((throwable, o) -> {
            ((EmitterCallback<DemoEntity>)o).error(throwable);
        })).subscribe();

        Mono<ResponseEntity<DemoEntity>> responseEntityMono = EmitterHandler.emits(
                emitterProcessor,
//                new DemoEntity(null)
                new DemoEntity("testing")
        ).map(output -> {
            return ResponseEntity.ok(output);
        }).onErrorReturn(
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build()
        );

        Disposable subscription = responseEntityMono.subscribe(
            responseEntity -> System.out.println(responseEntity.getStatusCode())
        );

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
