package com.jeffrey.example.demolib.shutdown.annotation;


import com.jeffrey.example.demolib.eventstore.annotation.EnableEventStore;
import com.jeffrey.example.demolib.shutdown.util.EnableGracefulShutdownImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EnableGracefulShutdownImportSelector.class)
@EnableEventStore
public @interface EnableGracefulShutdown {

}
