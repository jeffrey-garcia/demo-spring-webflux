package com.jeffrey.example.demoapp.repository;

import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DemoRxRepository extends ReactiveMongoRepository<DemoEntity, String> {

    Flux<DemoEntity> findAllByData(String data);

}
