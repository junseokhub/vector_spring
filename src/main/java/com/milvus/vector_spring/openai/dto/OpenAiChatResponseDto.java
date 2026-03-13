package com.milvus.vector_spring.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiChatResponseDto(
        String id,
        String object,
        Long created,
        String model,
        List<Choice> choices,
        OpenAiUsageResponseDto usage,
        String systemFingerprint
) {
    public record Choice(
            int index,
            Message message,
            String finishReason
    ) {}

    public record Message(
            String role,
            String content
    ) {}
}
