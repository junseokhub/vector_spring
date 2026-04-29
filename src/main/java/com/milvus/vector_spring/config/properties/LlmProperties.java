package com.milvus.vector_spring.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
        Provider openai,
        Provider ollama
) {
    public record Provider(String baseUrl) {}
}
