package com.jeffrey.example.demospringwebflux.dao;

import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import com.jeffrey.example.demospringwebflux.repository.DemoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@EnableMongoRepositories(basePackageClasses = DemoRepository.class)
public class DemoDao {

    @Autowired
    DemoRepository demoRepository;

    public DemoEntity createDemoEntity(DemoEntity demoEntity) {
        return demoRepository.save(demoEntity);
    }

    public Collection<DemoEntity> readAllDemoEntities(String sortBy) {
        if (DemoEntity.CREATED_ON.equals(sortBy)) {
            return demoRepository.findAll(Sort.by(Sort.Direction.ASC, DemoEntity.CREATED_ON));

        } else if (DemoEntity.DATA.equals(sortBy)) {
            return demoRepository.findAll(Sort.by(Sort.Direction.ASC, DemoEntity.DATA));

        } else if (DemoEntity.ID.equals(sortBy)) {
            return demoRepository.findAll(Sort.by(Sort.Direction.ASC, DemoEntity.ID));

        } else {
            // default ordering
            return demoRepository.findAll();
        }
    }

    public Optional<DemoEntity> readDemoEntityById(String id) {
        return demoRepository.findById(id);
    }

}
