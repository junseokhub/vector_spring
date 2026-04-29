package com.milvus.vector_spring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisProperties(
        Cluster cluster,
        String password
) {
    public record Cluster(List<String> nodes) {}
}
