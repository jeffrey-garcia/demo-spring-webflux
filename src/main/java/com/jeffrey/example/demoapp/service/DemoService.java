package com.jeffrey.example.demoapp.service;

import com.jeffrey.example.demoapp.dao.DemoDao;
import com.jeffrey.example.demoapp.entity.DemoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class DemoService {

    @Autowired
    DemoDao demoDao;

    public DemoEntity createDemoEntity(DemoEntity demoEntity) {
        return demoDao.createDemoEntity(demoEntity);
    }

    public Collection<DemoEntity> readAllDemoEntities(String sortBy) {
        return demoDao.readAllDemoEntities(sortBy);
    }

    public Optional<DemoEntity> readDemoEntityById(String id) {
        return demoDao.readDemoEntityById(id);
    }

}
