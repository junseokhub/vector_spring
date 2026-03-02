package com.milvus.vector_spring.util.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "common")
public record CommonProperties(
        int ivSize,
        String secretKey,
        String adminEmail,
        String adminPassword
        ) {
}
