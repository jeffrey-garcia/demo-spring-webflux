package com.jeffrey.example.demoapp.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Component
public class DemoProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoProcessor.class);

    @Bean
    public Function<Flux<String>, Flux<String>> wrapDoubleQuotes() {
        return flux -> flux.map(value -> {
            String transformedValue = String.format("\"%s\"", value);
            LOGGER.debug("Transformed - wrapDoubleQuotes: {}", transformedValue);
            return transformedValue;
        });
    }

    @Bean
    public Function<Flux<String>, Flux<String>> unwrapDoubleQuotes() {
        return flux -> flux.map(value -> {
            String transformedValue = value.replaceAll("\"", "");
            LOGGER.debug("Transformed - unwrapDoubleQuotes: {}", transformedValue);
            return transformedValue;
        });
    }

    @Bean
    public Function<String, String> toUpperCase() {
        return value -> {
            String transformedValue = value.toUpperCase();
            LOGGER.debug("Transformed - toUpperCase: {}", transformedValue);
            return transformedValue;
        };
    }

    @Bean
    public Function<String, String> toLowerCase() {
        return value -> {
            String transformedValue = value.toLowerCase();
            LOGGER.debug("Transformed - toLowerCase: {}", transformedValue);
            return transformedValue;
        };
    }

}
