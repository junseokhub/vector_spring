package com.milvus.vector_spring.llm.dto;

import com.milvus.vector_spring.llm.LlmPlatform;

public record ChatCompletionRequestDto(
        LlmPlatform platform,
        String apiKey,
        String model,
        String userMessage,
        String systemMessage
) {
    public static ChatCompletionRequestDto from(LlmPlatform platform, String apiKey, String model, String userMessage, String systemMessage) {
        return new ChatCompletionRequestDto(platform, apiKey, model, userMessage, systemMessage);
    }
}
