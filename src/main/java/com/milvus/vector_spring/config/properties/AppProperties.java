package com.milvus.vector_spring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppProperties(
        int ivSize,
        String secretKey,
        String adminEmail,
        String adminPassword
) {}
