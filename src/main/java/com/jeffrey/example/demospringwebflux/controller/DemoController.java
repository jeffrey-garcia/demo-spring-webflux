package com.jeffrey.example.demospringwebflux.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
public class DemoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    DemoService demoService;

    /**
     * StreamBridge bean which allows us to send data to an output binding
     # effectively bridging non-stream application with spring-cloud-stream
     */
    @Autowired
    StreamBridge streamBridge;

    /**
     * curl  -i -X GET "http://localhost:8081/demoEntity/id"
     */
    @GetMapping(path = "/demoEntity/{id}")
    public ResponseEntity<DemoEntity> readDemoEntitiesByPathVariableId(
            @PathVariable(value = "id") String id) throws JsonProcessingException {
        Optional<DemoEntity> demoEntityOptional = demoService.readDemoEntityById(id);

        // enable client-side cache control, works in Safari but not Chrome (unless not using spring-webflux)
        return demoEntityOptional.map(demoEntity ->
                ResponseEntity
                    .ok()
                    .cacheControl(CacheControl
                                    .maxAge(1800, TimeUnit.SECONDS)
                                    .cachePublic()
                                    .mustRevalidate())
                    .body(demoEntity))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * curl -i -X GET "http://localhost:8081/demoEntities"
     * curl -i -X GET "http://localhost:8081/demoEntities?sortBy=createdOn"
     */
    @GetMapping(path = "/demoEntities")
    public ResponseEntity<Collection<DemoEntity>> readAllDemoEntities(
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy)
    {
        Collection<DemoEntity> demoEntityCollection = demoService.readAllDemoEntities(sortBy);

        // enable client-side cache control, works in Safari but not Chrome (unless not using spring-webflux)
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl
                                .maxAge(1800, TimeUnit.SECONDS)
                                .cachePublic()
                                .mustRevalidate())
                .body(demoEntityCollection);
    }

    /**
     * curl -i -X POST "http://localhost:8081/demoEntity"
     * curl -i -X POST 'http://localhost:8081/demoEntity' -H "Content-Type: application/json" -d '{"data":"abc"}'
     */
    @PostMapping(path = "/demoEntity")
    public ResponseEntity<DemoEntity> createDemoEntityByJson(
            @RequestBody(required = false) DemoEntity demoEntity)
    {
        DemoEntity savedEntity = demoService.createDemoEntity(demoEntity == null ? new DemoEntity(null) : demoEntity);

        // the binding name is auto-created by the configuration spring.cloud.stream.source
        streamBridge.send("supplier0-out-0", savedEntity.toString());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedEntity);
    }

    /**
     * curl  -i -X POST "http://localhost:8081/demoEntity" -H "Content-Type: application/x-www-form-urlencoded" -d "data=abc"
     */
    @PostMapping(path="/demoEntity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<DemoEntity>> createDemoEntityByFormPost(
            ServerWebExchange serverWebExchange) throws InterruptedException
    {
        /**
         * Process the request in specific scheduler and avoid blocking on the main
         * processing/event loop threads.
         *
         * If you invokeReactive blocking libraries without scheduling that work on a specific scheduler,
         * those calls will block one of the few threads available (by default, the Netty event
         * loop) and your application will only be able to serve a few requests concurrently
         * until hitting the maximum number of CPU cores.
         *
         * Parallel scheduler is primarily designed for CPU bound tasks, meaning its limited
         * by the max number of CPU cores. In this case, it's like setting your threadpool size
         * to the number of cores on a regular Servlet container. As such the app won't be able
         * to process a large number of concurrent requests.
         *
         * The whole point of using a reactive paradigm (and therefore util, and by extension
         * its Mono and Flux objects) is that it enables you to code in a non-blocking way, meaning
         * that the current thread of execution isn't "held up" waiting for the mono to emit a
         * value.
         *
         * If the ultimate goal is performance and scalability, wrapping blocking calls in a
         * reactive app is likely to perform worse than regular Servlet container, and hence the
         * design rationale:
         * - use Spring MVC and blocking return types when dealing with a blocking library (JPA)
         * - use Mono and Flux return types when not tied to any blocking library
         */
        return serverWebExchange.getFormData().subscribeOn(Schedulers.elastic()).map(
            _formData -> demoService.createDemoEntity(new DemoEntity(_formData.getFirst("data"))) // blocking DB write goes here
        ).map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

}
