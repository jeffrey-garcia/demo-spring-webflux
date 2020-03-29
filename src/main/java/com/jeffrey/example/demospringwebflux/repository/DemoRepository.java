package com.jeffrey.example.demospringwebflux.repository;


import com.jeffrey.example.demospringwebflux.entity.DemoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemoRepository extends MongoRepository<DemoEntity, String> {

    Optional<DemoEntity> findById(String id);

}
