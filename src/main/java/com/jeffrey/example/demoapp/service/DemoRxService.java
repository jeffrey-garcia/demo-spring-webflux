package com.jeffrey.example.demoapp.service;

import com.jeffrey.example.demoapp.dao.DemoRxDao;
import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DemoRxService {

    @Autowired
    DemoRxDao demoRxDao;

    public Mono<DemoEntity> createDemoEntity(DemoEntity demoEntity) {
        return demoRxDao.createDemoEntity(demoEntity);
    }

    public Flux<DemoEntity> createDemoEntities(Iterable<DemoEntity> demoEntities) {
        return demoRxDao.createDemoEntities(demoEntities);
    }

    public Flux<DemoEntity> readAllDemoEntities(String sortBy) {
        return demoRxDao.readAllDemoEntities(sortBy);
    }

    public Mono<DemoEntity> readDemoEntityById(String id) {
        return demoRxDao.readDemoEntityById(id);
    }

}
