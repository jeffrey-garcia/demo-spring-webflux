package com.jeffrey.example.demospringwebflux.service;

import com.jeffrey.example.demospringwebflux.dao.CustomerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    CustomerDao customerDao;



}
