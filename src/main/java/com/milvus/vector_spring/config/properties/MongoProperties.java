package com.milvus.vector_spring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.mongodb")
public record MongoProperties(
        String database,
        String uri,
        String authenticationDatabase,
        boolean autoIndexCreation
) {}
