package com.jeffrey.example.demospringwebflux.dao;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.repository.DemoRxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@EnableReactiveMongoRepositories(basePackageClasses = DemoRxRepository.class)
public class DemoRxDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRxDao.class);

    @Autowired
    DemoRxRepository demoRxRepository;

    public Mono<DemoEntity> createDemoEntity(DemoEntity demoEntity) {
        return demoRxRepository.save(demoEntity);
    }

    public Flux<DemoEntity> createDemoEntities(Iterable<DemoEntity> demoEntities) {
        final AtomicInteger i = new AtomicInteger(0);
        Flux<DemoEntity> savedEntitiesFlux = Flux.fromIterable(demoEntities)
                                                .flatMap(demoEntity -> {
                                                    LOGGER.debug("saving entity: " + demoEntity.toString());
                                                    return demoRxRepository.save(demoEntity);
                                                                        // Simulate exception thrown during individual write to DB!
//                                                                        .map(_demoEntity -> {
//                                                                            if (i.addAndGet(1)%2 == 0) {
//                                                                                demoRxRepository.delete(_demoEntity).subscribe();
//                                                                                throw new RuntimeException("error writing to DB: " + _demoEntity.toString());
//                                                                            }
//                                                                            return _demoEntity;
//                                                                        }).onErrorStop();
                                                });

        return savedEntitiesFlux;
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
