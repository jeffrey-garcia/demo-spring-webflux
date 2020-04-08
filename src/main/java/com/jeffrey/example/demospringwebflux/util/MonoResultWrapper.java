package com.jeffrey.example.demospringwebflux.util;

public class MonoResultWrapper<T> {

    private T result = null;
    private Throwable throwable = null;

    public void setResult(T result) {
        this.result = result;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public T getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
