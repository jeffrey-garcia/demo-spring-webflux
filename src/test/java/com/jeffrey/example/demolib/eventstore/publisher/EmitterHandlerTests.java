package com.jeffrey.example.demolib.eventstore.publisher;

import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class EmitterHandlerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmitterHandlerTests.class);

    @Test
    public void verifySubscriptionBeforeOnSubscribe() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);

        final Message<DemoEntity> message =  MessageBuilder
                                                .withPayload(new DemoEntity(null))
                                                .setHeader("eventId", UUID.randomUUID().toString()).build();

        final Mono<?> callback = EmitterHandler.create(message);

        // subscribe to the callback and create the subscription
        final Disposable subscription = callback.subscribeOn(Schedulers.boundedElastic()).subscribe();

        // onSubscribe will never get notified because it
        // is registered AFTER the subscription is created
        callback.doOnSubscribe(_subscription -> {
            EmitterHandler.notifySuccess(message); // can never happen
        })
        .doFinally(signalType -> {
            subscription.dispose();
            lock.countDown(); // can never happen
        });

        lock.await(3000, TimeUnit.MILLISECONDS);
        long pending = lock.getCount();
        Assert.assertEquals(1, pending);
    }

    @Test
    public void verifySubscriptionAfterOnSubscribe() throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);

        final Message<DemoEntity> message =  MessageBuilder
                .withPayload(new DemoEntity(null))
                .setHeader("eventId", UUID.randomUUID().toString()).build();

        final Mono<?> callback = EmitterHandler.create(message);
        callback.doOnSubscribe(_subscription -> {
            EmitterHandler.notifySuccess(message); // can never happen
        })
        .doFinally(signalType -> {
            lock.countDown(); // can never happen
        })
        .timeout(Duration.ofMillis(1000))
        .doOnNext(output -> {
            Assert.assertEquals(message, output);
        })
        .doOnError(throwable -> {
            Assert.fail(throwable.getMessage());
        })
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();

        lock.await(3000, TimeUnit.MILLISECONDS);
        long pending = lock.getCount();
        Assert.assertEquals(0, pending);
    }

    @Test
    public void verifyCallbackWithConcurrentAccess_multipleCycles() {
        final int TEST_CYCLE = 100;

        for (int i=0; i<(TEST_CYCLE); i++) {
            verifyCallbackWithConcurrentAccess();
        }
    }

//    @Test
    private void verifyCallbackWithConcurrentAccess() {
        final int TEST_PER_CYCLE = 1000;
        final int MAX_THREAD = 4;
        final CountDownLatch lock = new CountDownLatch(TEST_PER_CYCLE);
        final Executor executor = Executors.newFixedThreadPool(MAX_THREAD);

        for (int i=0; i<(TEST_PER_CYCLE); i++) {
            final DemoEntity demoEntity = new DemoEntity(String.valueOf(i));
            executor.execute(() -> {
                Mono<?> callback = EmitterHandler.create(demoEntity);
                callback.doOnSubscribe(subscription -> {
                    Message<DemoEntity> message = MessageBuilder
                                                    .withPayload(demoEntity)
                                                    .setHeader("eventId", UUID.randomUUID().toString()).build();
                    EmitterHandler.transform(demoEntity, message);
                    EmitterHandler.notifySuccess(message);
                })
                .doFinally(signalType -> {
                    lock.countDown();
                })
                .doOnNext(output -> {
                    Assert.assertEquals(output, demoEntity);
                })
                .doOnError(throwable -> {
                    Assert.fail(throwable.getMessage());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
            });
        }

        try {
            lock.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) { }

        Assert.assertEquals(0, lock.getCount());
    }

}
