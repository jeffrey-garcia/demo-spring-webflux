package com.jeffrey.example.demoapp.function;

import com.jeffrey.example.demoapp.entity.DemoEntity;
import com.jeffrey.example.demolib.eventstore.aop.advice.ConsumerAdviceInvocator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@RunWith(JUnit4.class)
public class DemoConsumerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoConsumerTests.class);

    private static DemoConsumer demoConsumer;

    @BeforeClass
    public static void setUp() {
        demoConsumer = new DemoConsumer();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void verifyConsumerRx0() {
        int TEST_COUNT = 5;

        // declare the input test data and convert to stream
        Message<DemoEntity> message = MessageBuilder.withPayload(new DemoEntity("testing")).build();
        Flux<Message<DemoEntity>> inputFlux = Flux.create((FluxSink<Message<DemoEntity>> sink) -> {
            for (int i=0; i<TEST_COUNT; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sink.next(message);
            }

            // notify flux completion when all data has been processed
            sink.complete();
        });

        // declare the consumer
        Consumer<Flux<Message<DemoEntity>>> consumerRx0 = demoConsumer.consumerRx0();

        // declare the result
        List<Message<DemoEntity>> output = new ArrayList<>();

        // emit the stream to consumer
        consumerRx0.andThen(flux -> {
            // output the consumed data for validation
            flux.doOnNext(value -> {
                LOGGER.debug("result: {}", value);
                output.add(value);
            }).subscribe().dispose();
        }).accept((Flux<Message<DemoEntity>>) ConsumerAdviceInvocator.invokeReactive(inputFlux));

        // validate the output
        Assert.assertEquals(TEST_COUNT, output.size());
        for (int i=0; i<TEST_COUNT; i++) {
            Assert.assertEquals(message, output.get(i));
        }
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void verifyConsumerRx1() {
        // declare the input test data and convert to stream
        String [] input = new String[] {"a","b","c"};
        Flux<String> inputFlux = Flux.fromArray(input);

        // declare the consumer
        Consumer<Flux<String>> consumerRx1 = demoConsumer.consumerRx1();

        // declare the result
        List<String> output = new ArrayList<>();

        // emit the stream to consumer
        consumerRx1.andThen(flux -> {
            // output the consumed data for validation
            flux.doOnNext(value -> {
                LOGGER.debug("result: {}", value);
                output.add(value);
            }).subscribe().dispose();
        }).accept((Flux<String>) ConsumerAdviceInvocator.invokeReactive(inputFlux));

        // validate the output
        Assert.assertEquals(input.length, output.size());
        for (int i=0; i<input.length; i++) {
            Assert.assertEquals(input[i], output.get(i));
        }
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void verifyConsumerRx2() {
        // declare the input test data and convert to stream
        String [] input = new String[] {"a","b","c"};
        Flux<String> inputFlux = Flux.fromArray(input);

        // declare the consumer
        Function<Flux<String>, Flux<String>> consumerRx2 = demoConsumer.consumerRx2();

        // declare the result
        List<String> output = new ArrayList<>();

        // emit the stream to consumer
        Flux<String> outputFlux = consumerRx2.apply((Flux<String>) ConsumerAdviceInvocator.invokeReactive(inputFlux));

        // output the consumed data for validation
        outputFlux.doOnNext(value -> {
            LOGGER.debug("result: {}", value);
            output.add(value);
        }).subscribe().dispose();

        // validate the output
        Assert.assertEquals(input.length, output.size());
        for (int i=0; i<input.length; i++) {
            Assert.assertEquals(input[i], output.get(i));
        }
    }

    @Test
    public void verifyConsumer0() {
        // declare the input test data
        String input = "testing";

        // declare the consumer
        Consumer<String> consumer0 = demoConsumer.consumer0();

        // emit the data to consumer
        consumer0.andThen(value -> {
            // validate the output
            Assert.assertEquals(input, value);
        }).accept(ConsumerAdviceInvocator.invoke(input));
    }

    @Test
    public void verifyConsumer1() {
        // declare the input test data
        String input = "testing";

        // declare the consumer
        Function<String, String> consumer1 = demoConsumer.consumer1();

        // emit the data to consumer
        String output = consumer1.apply(ConsumerAdviceInvocator.invoke(input));

        // validate the output
        Assert.assertEquals(input, output);
    }

}
