package com.jeffrey.example.demoapp.config;

import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.EmitterProcessor;

@Configuration
public class DemoConfig {

    @Bean(name = "demoEntityEmitProcessor")
    public EmitterProcessor<DemoEntity> demoEntityEmitProcessor() {
        return EmitterProcessor.create();
    }

}
