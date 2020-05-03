package com.jeffrey.example.demolib.eventstore.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.util.Assert;

@Configuration
public class MongoDbConfig extends AbstractMongoClientConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbConfig.class);

    @Value("${spring.data.mongodb.uri:#{null}}")
    protected String mongoDbConnectionString;

    @Bean("mongoDbFactory")
    @Override
    public SimpleMongoClientDbFactory mongoDbFactory() {
        Assert.notNull(mongoDbConnectionString, "mongoDbConnectionString is null");
        ConnectionString connectionString = new ConnectionString(mongoDbConnectionString);
        return new SimpleMongoClientDbFactory(connectionString);
    }

    @Override
    protected String getDatabaseName() {
        String dbName = mongoDbFactory().getDb().getName();
        return dbName;
    }

    @Override
    public MongoClient mongoClient() {
        Assert.notNull(mongoDbConnectionString, "mongoDbConnectionString is null");
        ConnectionString connectionString = new ConnectionString(mongoDbConnectionString);
        MongoClient mongoClient = MongoClients.create(connectionString);
        return mongoClient;
    }

    @Override
    public boolean autoIndexCreation() {
        // Automatic index creation will be turned OFF by default with the release of 3.x.
        // Let index creation to happen either out of band or as part of the application
        // startup using IndexOperations.
        return false;
    }
}
