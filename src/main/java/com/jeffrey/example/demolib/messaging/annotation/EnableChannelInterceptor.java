package com.jeffrey.example.demolib.messaging.annotation;

import com.jeffrey.example.demolib.messaging.util.EnableChannelInterceptorImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableChannelInterceptorImportSelector.class})
public @interface EnableChannelInterceptor {

    @SuppressWarnings("unused")
    boolean useDefault() default true;

}
