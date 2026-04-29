package com.milvus.vector_spring.config.mongo;

import com.milvus.vector_spring.config.properties.MongoProperties;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
@RequiredArgsConstructor
public class MongoClientConfig extends AbstractMongoClientConfiguration {

    private final MongoProperties mongoProperties;

    @Bean
    @Override
    @NotNull
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoProperties.uri());
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    @NotNull
    protected String getDatabaseName() {
        return mongoProperties.database();
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), getDatabaseName()));
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
