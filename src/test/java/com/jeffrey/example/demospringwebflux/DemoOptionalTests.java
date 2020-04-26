package com.jeffrey.example.demospringwebflux;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

@RunWith(JUnit4.class)
public class DemoOptionalTests {

    @Test
    public void verifyEmptyOrNull() {
        Optional<String> optional = Optional.empty();
        Assert.assertEquals(false, optional.isPresent());
        optional.ifPresent(s -> {
            Assert.fail("should not be present!");
        });
    }

    @Test
    public void verifyNullable() {
        long random = System.currentTimeMillis();
        String s = random%2==0 ? s = "test": null;
        Optional<String> optional = Optional.ofNullable(s);
        Assert.assertEquals(random % 2 == 0, optional.isPresent());
    }

}
