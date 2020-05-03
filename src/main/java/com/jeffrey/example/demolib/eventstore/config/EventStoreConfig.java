package com.jeffrey.example.demolib.eventstore.config;

import com.jeffrey.example.demolib.eventstore.aop.aspect.ReactiveEventStoreAspect;
import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.EmitterProcessor;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * There are 2 ways to register aspect classes:
 * 1) manually register aspect classes as regular beans, or
 * 2) autodetect them through classpath scanning 
 *
 * If you opt for autodetect through classpath scanning,
 * you are required to add a separate @Component annotation
 * to the aspect class together with @Aspect annotation
 */
//@ComponentScan("com.example.jeffrey.demolib.eventstore.aop.aspect")
@EnableAspectJAutoProxy
@Configuration
public class EventStoreConfig {

    @Bean
    public RouterFunction<ServerResponse> htmlRouter(@Value("classpath:/static/index.html") Resource html) {
        return route(GET("/"), request
                -> ok().contentType(MediaType.TEXT_HTML).bodyValue(html)
        );
    }

    @Bean(name = "demoEntityEmitProcessor")
    public EmitterProcessor<DemoEntity> demoEntityEmitProcessor() {
        return EmitterProcessor.create();
    }

    /**
     * manually register aspect class as regular bean
     */
    @Bean
    public ReactiveEventStoreAspect demoAspect() {
        return new ReactiveEventStoreAspect();
    }
}
