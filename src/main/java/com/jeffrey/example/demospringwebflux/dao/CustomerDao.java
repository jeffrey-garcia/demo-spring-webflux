package com.jeffrey.example.demospringwebflux.dao;

import com.jeffrey.example.demospringwebflux.dao.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

@Component
@EnableMongoRepositories
public class CustomerDao {

    @Autowired
    CustomerRepository customerRepository;



}
