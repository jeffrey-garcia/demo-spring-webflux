package com.jeffrey.example.demospringwebflux.dao;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.repository.DemoRxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@EnableReactiveMongoRepositories(basePackageClasses = DemoRxRepository.class)
public class DemoRxDao {

    @Autowired
    DemoRxRepository demoRxRepository;

    public Mono<DemoEntity> createDemoEntity(DemoEntity demoEntity) {
        return demoRxRepository.save(demoEntity);
    }

    public Flux<DemoEntity> readAllDemoEntities(String sortBy) {
        if (DemoEntity.CREATED_ON.equals(sortBy)) {
            return demoRxRepository.findAll(Sort.by(Sort.Direction.ASC, DemoEntity.CREATED_ON));

        } else if (DemoEntity.DATA.equals(sortBy)) {
            return demoRxRepository.findAll(Sort.by(Sort.Direction.ASC, DemoEntity.DATA));

        } else if (DemoEntity.ID.equals(sortBy)) {
            return demoRxRepository.findAll(Sort.by(Sort.Direction.ASC, DemoEntity.ID));

        } else {
            // default ordering
            return demoRxRepository.findAll();
        }
    }

    public Mono<DemoEntity> readDemoEntityById(String id) {
        return demoRxRepository.findById(id);
    }


}
