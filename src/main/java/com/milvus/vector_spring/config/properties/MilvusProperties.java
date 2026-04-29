package com.milvus.vector_spring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "milvus")
public record MilvusProperties(
        String username,
        String password,
        String token,
        String clusterEndpoint,
        String collectionName,
        double scoreThreshold
) {}
