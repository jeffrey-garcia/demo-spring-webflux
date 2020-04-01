package com.jeffrey.example.demospringwebflux;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class DemoTests {

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
    public void verifyFluxError() {
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
}
