package com.jeffrey.example.demolib.eventstore.config;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.util.Assert;

/**
 * Event store database configuration for reactive MongoDB
 *
 * @author Jeffrey Garcia Wong
 */
@Configuration
public class ReactiveMongoDbConfig extends AbstractReactiveMongoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveMongoDbConfig.class);

    @Value("${spring.data.mongodb.uri:#{null}}")
    protected String mongoDbConnectionString;

    @Bean("reactiveMongoDbFactory")
    @Override
    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        Assert.notNull(mongoDbConnectionString, "mongoDbConnectionString is null");
        ConnectionString connectionString = new ConnectionString(mongoDbConnectionString);
        return new SimpleReactiveMongoDatabaseFactory(connectionString);
    }

    @Override
    protected String getDatabaseName() {
        String dbName = reactiveMongoDbFactory().getMongoDatabase().getName();
        return dbName;
    }

    @Override
    public MongoClient reactiveMongoClient() {
        Assert.notNull(mongoDbConnectionString, "mongoDbConnectionString is null");
        ConnectionString connectionString = new ConnectionString(mongoDbConnectionString);
        MongoClient mongoClient = MongoClients.create(connectionString);
        return mongoClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        return super.mappingMongoConverter();
    }

}
