package com.milvus.vector_spring.util.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.ai.ollama")
public record OllamaProperties(
        String baseUrl,
        Embedding embedding
) {
    public record Embedding(
            Options options
    ) {}

    public record Options(
            String model
    ) {}
}
