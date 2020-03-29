package com.jeffrey.example.demospringwebflux;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class DemoTests {

    @Test
    public void test() {
        Assert.assertEquals("1", getMono());
    }

    public String getMono() {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.put("A", Arrays.asList("1","2","3"));

        Mono<MultiValueMap<String, String>> formData = Mono.just(multiValueMap);
        Mono<String> firstElement = formData.publishOn(Schedulers.elastic()).map(_data -> {
            return _data.getFirst("A");
        }).map(_firstElement -> {
            System.out.println(_firstElement);
            return _firstElement;
        });

        return firstElement.block();
    }

    public List<Integer> getFlux() {
        Flux<Integer> integerFlux = Flux.just(1,2,3,4);
        return integerFlux.collectList().block();
    }

}