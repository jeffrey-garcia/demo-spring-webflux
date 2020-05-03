package com.jeffrey.example.demoapp.annotation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class DemoAnnotationTests {

    @Test
    public void verifyAnnotationValueForAnonymous() throws ClassNotFoundException {

        Consumer<String> consumer = new Consumer<String>() {
            @Override
            @DemoAnnotation("anonymousClass")
            public void accept(String s) { }
        };

        Class<?> anonymousConsumer = consumer.getClass();
        Method[] methods = anonymousConsumer.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(DemoAnnotation.class)) {
                DemoAnnotation annotation = method.getAnnotation(DemoAnnotation.class);
                Assert.assertEquals("anonymousClass", annotation.value());
            }
        }
    }

    @Test
    public void verifyAnnotationValueForLambdas() throws ClassNotFoundException {
        Consumer<String> consumer = s -> {};

        Class anonymousConsumer = consumer.getClass();
        Method[] methods = anonymousConsumer.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(DemoAnnotation.class)) {
                DemoAnnotation annotation = method.getAnnotation(DemoAnnotation.class);
                Assert.assertEquals("lambdasConsumer", annotation.value());
            }
        }
    }

}
