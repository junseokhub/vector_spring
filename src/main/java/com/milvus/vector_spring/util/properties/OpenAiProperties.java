package com.milvus.vector_spring.util.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "open.ai")
public record OpenAiProperties(String url, String key, String embedUrl) {
}