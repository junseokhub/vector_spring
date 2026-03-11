package com.milvus.vector_spring.util.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaProperties (
        String bootstrapServers,
        Consumer consumer,
        String topic
) {
    public record Consumer (
            String groupId,
            String autoOffsetReset
    ) {}
}
