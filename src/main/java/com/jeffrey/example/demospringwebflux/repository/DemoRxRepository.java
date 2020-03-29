package com.jeffrey.example.demospringwebflux.repository;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DemoRxRepository extends ReactiveMongoRepository<DemoEntity, String> {

    Flux<DemoEntity> findAllByData(String data);

}
