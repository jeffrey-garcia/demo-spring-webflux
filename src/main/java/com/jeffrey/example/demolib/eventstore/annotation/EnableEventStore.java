package com.jeffrey.example.demolib.eventstore.annotation;

import com.jeffrey.example.demolib.eventstore.util.EnableEventStoreImportSelector;
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
@Import({EnableEventStoreImportSelector.class})
@EnableAutoConfiguration
public @interface EnableEventStore {

    // TODO: depends on the DB storage specified by user
    // TODO: add support for JPA

    // Disabling specific Mongo Auto-configuration Classes
    @SuppressWarnings("unused")
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

    // Indicate the usage of legacy SCSt annotation-based bindings configuration
    // such as @EnableBinding, @Publisher, @StreamListener
    @SuppressWarnings("unused")
    boolean useLegacy() default false;
}
