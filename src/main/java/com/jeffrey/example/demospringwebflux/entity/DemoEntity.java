package com.jeffrey.example.demospringwebflux.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.ZonedDateTime;

@Document(collection = "DemoEntity") // specify the name of the collection in MongoDB
public class DemoEntity {

    public static final String ID = "id";
    public static final String CREATED_ON = "createdOn";
    public static final String DATA = "data";

    @Id
    @JsonProperty(ID)
    private String id;

    @JsonProperty(CREATED_ON)
    private Instant createdOn;

    @JsonProperty(DATA)
    private String data;

    private DemoEntity() {
        this.createdOn = ZonedDateTime.now().toInstant();
    }

    public DemoEntity(String data) {
        this();
        this.data = data;
    }

    public String toString() {
        return String.format("id: %s, createdOn: %s, data: %s", id, createdOn.toString(), data);
    }

    public String getData() {
        return data;
    }

}
