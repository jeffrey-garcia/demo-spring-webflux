package com.jeffrey.example.demospringwebflux.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Customer") // specify the name of the collection in MongoDB
public class Customer {

    // The id is mostly for internal use by MongoDB
    @Id
    public String id;

    public String firstName;
    public String lastName;

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
