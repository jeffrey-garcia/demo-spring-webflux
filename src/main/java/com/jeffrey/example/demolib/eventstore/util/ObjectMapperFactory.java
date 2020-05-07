package com.jeffrey.example.demolib.eventstore.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;

/**
 * Factory class which returns an object mapper for JSON serializing/de-serializing
 */
public class ObjectMapperFactory {

    /**
     * Return a singleton of {@link Jackson2JsonObjectMapper}
     *
     * @return {@link Jackson2JsonObjectMapper}
     */
    public static Jackson2JsonObjectMapper getMapper() {
        return LazyLoader.instance;
    }

    private static class LazyLoader {
        private static final Jackson2JsonObjectMapper instance = new Jackson2JsonObjectMapper(new ObjectMapper());
    }

}