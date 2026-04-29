package com.milvus.vector_spring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(Token token) {

    public record Token(
            String secretKey,
            int accessExpiration,
            int refreshExpiration
    ) {}
}
