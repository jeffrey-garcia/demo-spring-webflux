package com.jeffrey.example.demoapp.aop;

import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

public class DemoAopTests {

    private static class MonoResultWrapper<T> {
        private T result;
        private Throwable throwable;
        public void setResult(T result) {
            this.result = result;
        }
        public T getResult() {
            return this.result;
        }
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }
        public Throwable getThrowable() {
            return this.throwable;
        }
    }

    private MonoResultWrapper<Boolean> throwableWrapper = new MonoResultWrapper<>();

    private Mono<String> test1() {
        return Mono.just("1");
    }

    private AtomicBoolean test0() {
        Mono<Boolean> resultMono = test1().map(_value -> {
            return true;
        })
        .onErrorMap(throwable -> {
            return throwable;
        })
        .onErrorReturn(false)
        .onErrorStop();

        AtomicBoolean result = new AtomicBoolean();
        resultMono.doOnNext(_result -> {
            System.out.println(_result);
            result.set(_result);
        }).subscribe();

        return result;
    }

    @Test
    public void test() {
        Assert.assertTrue(test0().get());
    }

    @Test
    public void verifyMonoErrorHandle() {
        Mono<DemoEntity> inputMono = Mono.just(
                new DemoEntity("1")
        );

        Mono<Boolean> outputMono = function2(function1(inputMono));
//        Mono<Boolean> outputMono = function1(inputMono);

        outputMono.doOnNext(value -> {
            Assert.assertFalse(value);
        }).subscribe();

        if (throwableWrapper.getThrowable() != null) {
            Assert.assertEquals(null, throwableWrapper.getResult());
            Assert.assertNotNull(throwableWrapper.getThrowable().getMessage());
            // if function 1 encounter error
            if (throwableWrapper.getThrowable().getMessage().contains("error 1")) {
                Assert.assertEquals("error 1", throwableWrapper.getThrowable().getMessage());
            } else if (throwableWrapper.getThrowable().getMessage().contains("error 2")) {
                Assert.assertEquals("error 2", throwableWrapper.getThrowable().getMessage());
            } else {
                Assert.fail("unknown throwable: " + throwableWrapper.getThrowable().getMessage());
            }
        } else {
            // no error/exception thrown, verify result
            Assert.assertEquals(null, throwableWrapper.getThrowable());
            Assert.assertNotNull(throwableWrapper.getResult());
            Assert.assertEquals(true, throwableWrapper.getResult());
        }
    }

    @Test
    public void verifyComposeFunction() {
        try {
            Object object = composeFunction();
            Assert.assertTrue(object instanceof Boolean);
            Assert.assertTrue((Boolean)object);
            System.out.println("compose function finish without error");
        } catch (Throwable e) {
            Assert.assertTrue(e.getMessage().indexOf("error") >= 0);
            System.out.println("compose function finish with error");
        }
    }

    private Object composeFunction() throws Throwable {
        Mono<DemoEntity> inputMono = Mono.just(
                new DemoEntity("1")
        );

        Mono<Boolean> outputMono = function2(function1(inputMono));
        outputMono.subscribe().dispose();

        if (throwableWrapper.getThrowable() != null) {
            Assert.assertEquals(null, throwableWrapper.getResult());
            Assert.assertNotNull(throwableWrapper.getThrowable().getMessage());
            // if function 1 encounter error
            if (throwableWrapper.getThrowable().getMessage().contains("error 1")) {
                Assert.assertEquals("error 1", throwableWrapper.getThrowable().getMessage());
            } else if (throwableWrapper.getThrowable().getMessage().contains("error 2")) {
                Assert.assertEquals("error 2", throwableWrapper.getThrowable().getMessage());
            } else {
                Assert.fail("unknown throwable: " + throwableWrapper.getThrowable().getMessage());
            }
        } else {
            // no error/exception thrown, verify result
            Assert.assertEquals(null, throwableWrapper.getThrowable());
            Assert.assertNotNull(throwableWrapper.getResult());
            Assert.assertEquals(true, throwableWrapper.getResult());
        }

        if (throwableWrapper.getThrowable()!=null) {
            throw throwableWrapper.getThrowable();
        } else {
            return throwableWrapper.getResult();
        }
    }

    private Mono<Boolean> function1(Mono<?> mono) {
        return mono.map(value -> {
            System.out.println("function 1: " + value);
//            throw new RuntimeException("error 1");
            return value;
        })
        .onErrorMap(throwable -> {
            System.err.println("function 1: " + throwable.getMessage());
            throwableWrapper.setThrowable(throwable);
            return throwable;
        })
        .map(value -> {
            System.out.println("function 1 success");
            return true;
        })
        .onErrorReturn(
                false
        )
        .onErrorStop();
    }

    private Mono<Boolean> function2(Mono<Boolean> mono) {
        return mono.map(value -> {
            System.out.println("function 1 result: " + value);
            if (value) {
                System.out.println("proceed function 2");
//                throwableWrapper.setResult(true);
                throw new RuntimeException("error 2");
            }
            return value;
        })
        .onErrorMap(throwable -> {
            System.err.println("function 2: " + throwable.getMessage());
            throwableWrapper.setThrowable(throwable);
            return throwable;
        })
        .map(value -> {
            if (value) System.out.println("function 2 success");
            return value;
        })
        .onErrorReturn(
                false
        )
        .onErrorStop();
    }

}
