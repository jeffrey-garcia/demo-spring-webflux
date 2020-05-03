package com.jeffrey.example;

import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@RunWith(JUnit4.class)
public class DemoTests {

    @Test
    public void verifyClosureFunction() {
        Function<Void, Integer> counter = new Function<Void, Integer>() {
            private Integer count = 0;
            @Override
            public Integer apply(Void _void) {
                return ++count;
            }
        };

        Assert.assertEquals(Integer.valueOf(1), counter.apply(null));
        Assert.assertEquals(Integer.valueOf(2), counter.apply(null));
    }

    @Test
    public void verifyMonoToFlux() {
        Mono<List<Integer>> inputMonoList = Mono.just(Arrays.asList(1,2,3,4));
        Flux<Integer> outputFlux = inputMonoList.flatMapMany(values -> Flux.fromIterable(values));

        List<Integer> outputList = new ArrayList<>();
        outputFlux.doOnNext(value -> {
            outputList.add(value);
        }).subscribe().dispose();

        Assert.assertEquals(4, outputList.size());
    }

    @Test
    public void verifyFluxToMono() {
        Flux<Integer> inputFlux = Flux.just(1,2,3,4);

        Mono<List<Integer>> outputMono = inputFlux.collectList();

        List<Integer> outputList = new ArrayList<>();
        outputMono.doOnNext(value -> {
            outputList.addAll(value);
        }).subscribe().dispose();

        Assert.assertEquals(4, outputList.size());
    }

    @Test
    public void verifyFlattenedStreamsOfStreams() {
        Flux<Flux<Integer>> inputNestedFlux = Flux.just(Flux.just(1),Flux.just(2),Flux.just(3),Flux.just(4));
        Flux<Integer> flattenedFlux = inputNestedFlux.flatMap(integerMono -> {
            return integerMono;
        });

        List<Integer> outputList = new ArrayList<>();
        flattenedFlux.doOnNext(value -> {
            outputList.add(value);
        }).subscribe().dispose();

        Assert.assertEquals(4, outputList.size());
    }

    @Test
    public void verifyFluxErrorHandle() {
        Flux<Integer> inputFlux = Flux.just(1,2,3,4);
        Flux<Integer> outputFlux = multipleByTwo(inputFlux);

        Flux<ResponseEntity<Object>> result = outputFlux.map(value ->
            ResponseEntity.status(HttpStatus.CREATED).build()
        ).onErrorReturn(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        );

        List<HttpStatus> outputHttpResponse = new ArrayList<>();
        result.doOnNext(value -> {
            outputHttpResponse.add(value.getStatusCode());
        }).subscribe().dispose();

        Assert.assertEquals(3, outputHttpResponse.size());
        Assert.assertEquals(HttpStatus.CREATED, outputHttpResponse.get(0));
        Assert.assertEquals(HttpStatus.CREATED, outputHttpResponse.get(1));
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, outputHttpResponse.get(2));
    }


    private Flux<Integer> multipleByTwo(Flux<Integer> flux) {
        // do something
        return flux.map(i -> {
            if (i==3) { throw new RuntimeException("error!"); }
            return i * 2;
        }).onErrorMap(throwable -> {
            System.err.println(throwable.getMessage());
            return throwable;
        }).onErrorStop();
    }

    @Test
    public void verifyFluxErrorHandle2() {
        Flux<DemoEntity> inputFlux = Flux.just(
                new DemoEntity("1"),
                new DemoEntity("2"),
                new DemoEntity("3"),
                new DemoEntity("4")
        );

        Flux<DemoEntity> outputFlux = inputFlux.map(_demoEntity -> {
            if (_demoEntity.getData().equals("3")) throw new RuntimeException("error!");
            return _demoEntity;
        });

        RuntimeException runtimeException = new RuntimeException();
        AtomicBoolean result = new AtomicBoolean(true);

        outputFlux.onErrorMap(throwable -> {
            result.set(false);
            runtimeException.setStackTrace(throwable.getStackTrace());
            return throwable;
        })
        .onErrorStop()
        .map(value -> {
            return true;
        })
        .onErrorReturn(false)
        .subscribe()
        .dispose();

        Assert.assertEquals(false, result.get());
    }

    @Test
    public void verifyEmitProcessor() {
        EmitterProcessor<DemoEntity> emitterProcessor = EmitterProcessor.create();

        Flux<DemoEntity> flux1 = emitterProcessor.doOnNext(demoEntity -> {
            System.out.println("@@@: " + demoEntity);
        }).map(demoEntity -> {
            return demoEntity;
        });

        /**
         * if we subscribe the flux 2 times, the task defined when each data comes in will be fired 2 times!
         */
        Disposable subscription1 = flux1.subscribe();
        Disposable subscription2 = flux1.subscribe();

        emitterProcessor.onNext(new DemoEntity(null));

        subscription1.dispose();
        subscription2.dispose();
    }

    @Test
    public void verifyEmitProcessorWithDownStreamError() {
        EmitterProcessor<DemoEntity> emitterProcessor = EmitterProcessor.create();

        Flux<DemoEntity> flux1 = emitterProcessor.doOnNext(demoEntity -> {
            System.out.println(demoEntity);
        }).map(demoEntity -> {
            return demoEntity;
        });

        Disposable subscription1 = flux1.map(demoEntity -> {
            throw new RuntimeException("error");
        })
        .onErrorResume(throwable -> {
            return Flux.error(throwable);
        })
        .subscribe(
            /**
             * if we throw error within the transformed flux
             * we need to handle it at the subscriber level which
             * will terminate the flux stream
             */
            value -> System.out.println("yay"),
            error -> System.out.println("shit")
        );

        emitterProcessor.onNext(new DemoEntity(null));
        emitterProcessor.onNext(new DemoEntity(null));

        subscription1.dispose();
    }

    @Test
    public void verifyEmitProcessorWithError() {
        EmitterProcessor<DemoEntity> emitterProcessor = EmitterProcessor.create();

        Flux<DemoEntity> flux1 = emitterProcessor.doOnNext(demoEntity -> {
            if (demoEntity.getData() == null)
                throw new RuntimeException("error");
        })
        .onErrorContinue((throwable, value) -> {
            // skip the error data without terminating the stream processing
        });

        Disposable subscription1 = flux1.subscribe(
            value -> System.out.println(value)
        );

        emitterProcessor.onNext(new DemoEntity(null));
        emitterProcessor.onNext(new DemoEntity("test"));

        subscription1.dispose();
    }

}
