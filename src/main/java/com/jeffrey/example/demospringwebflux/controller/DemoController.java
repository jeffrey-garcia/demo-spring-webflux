package com.jeffrey.example.demospringwebflux.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
public class DemoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    DemoService demoService;

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
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy) {
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
            @RequestBody(required = false) DemoEntity demoEntity) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(demoService.createDemoEntity(
                        demoEntity == null ?
                                new DemoEntity(null) : demoEntity
                        )
                );
    }

    /**
     * curl  -i -X POST "http://localhost:8081/demoEntity" -H "Content-Type: application/x-www-form-urlencoded" -d "data=abc"
     */
    @PostMapping(path="/demoEntity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<DemoEntity>> createDemoEntityByFormPost(
            ServerWebExchange serverWebExchange) throws InterruptedException
    {
        Mono<MultiValueMap<String, String>> formData = serverWebExchange.getFormData();
        return formData.map(data -> {
            DemoEntity demoEntity = demoService.createDemoEntity(new DemoEntity(data.getFirst("data")));
            return ResponseEntity.status(HttpStatus.CREATED).body(demoEntity);
        });
    }

}
