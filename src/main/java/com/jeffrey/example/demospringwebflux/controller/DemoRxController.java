package com.jeffrey.example.demospringwebflux.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.service.DemoRxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rx")
public class DemoRxController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    @Qualifier("demoEntityEmitProcessor")
    EmitterProcessor<DemoEntity> demoEntityEmitterProcessor;

    @Autowired
    DemoRxService demoRxService;

    /**
     * curl  -i -X GET "http://localhost:8081/rx/demoEntity/id"
     */
    @GetMapping(path = "/demoEntity/{id}")
    public Mono<ResponseEntity<DemoEntity>> readDemoEntitiesByPathVariableId(
            @PathVariable(value = "id") String id) throws JsonProcessingException {
        Mono<DemoEntity> demoEntityMono = demoRxService.readDemoEntityById(id);

        return demoRxService.readDemoEntityById(id).map(demoEntity ->
                ResponseEntity
                    .ok()
                    .cacheControl(CacheControl
                                    .maxAge(1800, TimeUnit.SECONDS)
                                    .cachePublic()
                                    .mustRevalidate())
                    .body(demoEntity))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * curl -i -X GET "http://localhost:8081/rx/demoEntities"
     * curl -i -X GET "http://localhost:8081/rx/demoEntities?sortBy=createdOn"
     */
    @GetMapping(path = "/demoEntities", produces = {MediaType.TEXT_EVENT_STREAM_VALUE,"application/stream+json"})
    public Flux<DemoEntity> readAllDemoEntities(
            @RequestParam(value="sortBy", required=false, defaultValue="") String sortBy)
            throws JsonProcessingException
    {
        return demoRxService.readAllDemoEntities(sortBy);
    }

    /**
     * curl -i -X POST "http://localhost:8081/rx/demoEntity"
     * curl -i -X POST 'http://localhost:8081/rx/demoEntity' -H "Content-Type: application/json" -d '{"data":"abc"}'
     */
    @PostMapping(path = "/demoEntity")
    public Mono<ResponseEntity<DemoEntity>> createDemoEntityByJson(
            @RequestBody(required = false) DemoEntity demoEntity)
    {
        return demoRxService.createDemoEntity(
            demoEntity == null ? new DemoEntity(null):demoEntity
        ).map(_demoEntity -> {
            /**
             * Uses EmitterProcessor from the reactor API to effectively provide a bridge between
             * the actual event source (rest endpoint in this case) and spring-cloud-stream.
             */
            demoEntityEmitterProcessor.onNext(_demoEntity);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(_demoEntity);
        });
    }

    /**
     * curl -i -X POST "http://localhost:8081/rx/demoEntity" -H "Content-Type: application/x-www-form-urlencoded" -d "data=abc"
     */
    @PostMapping(path="/demoEntity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<DemoEntity>> createDemoEntityByFormPost(
            ServerWebExchange serverWebExchange)
    {
        Mono<MultiValueMap<String, String>> formData = serverWebExchange.getFormData();
        return formData.flatMap(data ->
                demoRxService.createDemoEntity(new DemoEntity(data.getFirst("data")))
        ).map(newDemoEntity ->
                ResponseEntity.status(HttpStatus.CREATED).body(newDemoEntity)
        );
    }

}
