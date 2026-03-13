package com.milvus.vector_spring.openai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiUsageResponseDto(
        int promptTokens,
        int completionTokens,
        int totalTokens
) {
}