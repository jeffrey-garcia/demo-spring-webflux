package com.jeffrey.example.demospringwebflux.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.publisher.EmitterHandler;
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
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
        LOGGER.debug("/demoEntities");
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
        LOGGER.debug("/demoEntity: {}", demoEntity);
        final DemoEntity _demoEntity = demoEntity==null? new DemoEntity(null):demoEntity;

        // Emitter may encounter error and a successful response will always be returned
//        demoEntityEmitterProcessor.onNext(_demoEntity); // fire new data to the stream
//        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body(_demoEntity));

        /**
         * handle if emitter and downstream encounter error
         **/
        // create the callback
        Mono<?> callback = EmitterHandler.create(_demoEntity);

        // subscribe the callback
        Disposable subscription = callback.subscribeOn(Schedulers.boundedElastic()).subscribe();

        return callback.doOnSubscribe(_subscription -> {
            // start publishing data after subscribed
            demoEntityEmitterProcessor.onNext(_demoEntity);
        }).doFinally(output -> {
            // dispose the subcription when finish
            subscription.dispose();
        })
        .timeout(
            // define timeout to avoid blocking the server if response can't be produced timely
            Duration.ofMillis(1000)
        )
        .map(output -> {
            // successful result handle
            return ResponseEntity.ok((DemoEntity)output);
        })
        .onErrorReturn(
            // failure result handle
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        );
    }

    /**
     * curl -i -X POST 'http://localhost:8081/rx/demoEntities' -H "Content-Type: application/json" -d '[{"data":"abc"},{"data":"def"}]'
     */
    @PostMapping(path = "/demoEntities")
    public Mono<ResponseEntity<Iterable<DemoEntity>>> createDemoEntitiesByJson(
            @RequestBody Iterable<DemoEntity> demoEntities)
    {
        List<DemoEntity> savedEntities = new ArrayList<>();

        Mono<List<DemoEntity>> savedEntitiesMono = demoRxService.createDemoEntities(demoEntities)
                .doOnNext(demoEntity -> {
                    LOGGER.debug("saved entity: " + demoEntity.toString());
                    savedEntities.add(demoEntity);
                    demoEntityEmitterProcessor.onNext(demoEntity);
                })
                .doOnError(throwable -> {
                    LOGGER.error("error: {}", throwable.getMessage());
                })
                .collectList();

        return savedEntitiesMono.map(_savedEntities ->
                                    ResponseEntity.status(HttpStatus.CREATED).body((Iterable<DemoEntity>)savedEntities)
                                ).onErrorReturn(
                                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((Iterable<DemoEntity>)savedEntities)
                                );
    }

    /**
     * curl -i -X POST "http://localhost:8081/rx/demoEntity" -H "Content-Type: application/x-www-form-urlencoded" -d "data=abc"
     */
    @PostMapping(path="/demoEntity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<ResponseEntity<DemoEntity>> createDemoEntityByFormPost(
            ServerWebExchange serverWebExchange)
    {
        Mono<MultiValueMap<String, String>> formData = serverWebExchange.getFormData();
        return formData.map(_formData -> {
                DemoEntity demoEntity = new DemoEntity(_formData.getFirst("data"));
                demoEntityEmitterProcessor.onNext(demoEntity);
                return demoEntity;
        }).map(newDemoEntity ->
                ResponseEntity.status(HttpStatus.CREATED).body(newDemoEntity)
        );
    }

}
