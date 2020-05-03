package com.jeffrey.example;

import com.google.common.collect.Maps;
import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.internal.util.collections.Sets;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.MonoProcessor;

import java.util.*;

@RunWith(JUnit4.class)
public class DemoGenericsTest {

    public static class A {
        private static final Map<? super Object, List<? super Object>> inputMap = Maps.newConcurrentMap();
        private static final Map<? super Object, List<? super Object>> outputMap = Maps.newConcurrentMap();

        public static <T> void input(Set<T> set, T data) {
            set.add(data);
            List<? super Object> list = Collections.singletonList(data);
            inputMap.put(data, list);
        }

        public static <T,R> void transform(T source, R transform) {
            List<? super Object> list = inputMap.remove(source);
            outputMap.put(transform, list);
        }

        public static <R> Object output(R transform) {
            return outputMap.remove(transform).get(0);
        }
    }

    @Test
    public void test() {
        Map<? super Object,? super Object> map = new HashMap<>();
        map.put("1", "1");
        map.put(Boolean.TRUE, Arrays.asList());
        map.put(new Object(), new Object());
    }

    @Test
    public void verifyA() {
        A.input(Sets.newSet(), "TEST");
        A.transform("TEST", Boolean.TRUE);
        Object object = A.output(Boolean.TRUE);
        Assert.assertTrue(object instanceof String);
        Assert.assertEquals("TEST", object);
    }

    public static class B {
        private static final Map<? super Object, MonoProcessor<? super Object>> inputCallbackMap = Maps.newConcurrentMap();
        private static final Map<? super Object, MonoProcessor<? super Object>> outputCallbackMap = Maps.newConcurrentMap();

        public static <T> MonoProcessor<? super Object> input(EmitterProcessor<T> emitterProcessor, T source) {
            MonoProcessor<? super Object> processor = MonoProcessor.create();
            inputCallbackMap.put(source, processor);
            emitterProcessor.onNext(source);
            return processor;
        }

        public static <T,R> void transform(T source, R transform) {
            outputCallbackMap.put(transform, inputCallbackMap.remove(source));
        }

        public static <R> void output(R transform) {
            outputCallbackMap.get(transform).onNext(transform);
        }
    }

    @Test
    public void verifyB() {
        EmitterProcessor<String> emitterProcessor = EmitterProcessor.create();

        String inputData = "INPUT";

        emitterProcessor.doOnNext(value -> {
            B.transform(value, "OUTPUT");
            B.output("OUTPUT");
        }).subscribe();

        MonoProcessor<? super Object> processor = B.input(emitterProcessor, inputData);
        processor.doOnNext(value -> {
            Assert.assertEquals("OUTPUT", value);
        }).subscribe();
    }

    @Test
    public void verifyB2() {
        EmitterProcessor<DemoEntity> emitterProcessor = EmitterProcessor.create();

        DemoEntity demoEntity = new DemoEntity("123");

        emitterProcessor.doOnNext(value -> {
            Message<DemoEntity> message = MessageBuilder.withPayload(demoEntity).build();
            B.transform(value, message);
            B.output(message);
        }).subscribe();

        MonoProcessor<? super Object> processor = B.input(emitterProcessor, demoEntity);
        processor.doOnNext(value -> {
            Assert.assertTrue(value instanceof Message);
            Assert.assertTrue(((Message<?>)value).getPayload() instanceof DemoEntity);
            Assert.assertEquals(demoEntity, ((Message<?>)value).getPayload());
        }).subscribe();
    }
}
