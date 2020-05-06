package com.jeffrey.example.demolib.shutdown.annotation;

import com.jeffrey.example.demolib.eventstore.annotation.EnableEventStore;
import com.jeffrey.example.demolib.messaging.annotation.EnableChannelInterceptor;
import com.jeffrey.example.demolib.shutdown.util.EnableGracefulShutdownImportSelector;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EnableGracefulShutdownImportSelector.class)
@EnableChannelInterceptor
@EnableEventStore
public @interface EnableGracefulShutdown {

    @AliasFor(
            annotation = EnableAutoConfiguration.class,
            attribute = "exclude"
    )
    Class<?>[] suppressAutoConfiguration() default {
            MongoAutoConfiguration.class,
            MongoDataAutoConfiguration.class,
            MongoReactiveAutoConfiguration.class,
            MongoReactiveDataAutoConfiguration.class
    };

    @AliasFor(
            annotation = EnableChannelInterceptor.class,
            attribute = "useDefault"
    )
    boolean useDefaultChannelInterceptor() default true;

}
