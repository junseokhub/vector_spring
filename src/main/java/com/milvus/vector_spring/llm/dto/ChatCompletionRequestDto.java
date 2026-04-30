package com.milvus.vector_spring.llm.dto;

import com.milvus.vector_spring.llm.LlmPlatform;

import java.util.List;

public record ChatCompletionRequestDto(
        LlmPlatform platform,
        String apiKey,
        String model,
        String userMessage,
        String systemMessage,
        List<ConversationTurn> history
) {
    public static ChatCompletionRequestDto from(
            LlmPlatform platform, String apiKey, String model,
            String userMessage, String systemMessage
    ) {
        return new ChatCompletionRequestDto(platform, apiKey, model, userMessage, systemMessage, List.of());
    }

    public static ChatCompletionRequestDto from(
            LlmPlatform platform, String apiKey, String model,
            String userMessage, String systemMessage, List<ConversationTurn> history
    ) {
        return new ChatCompletionRequestDto(platform, apiKey, model, userMessage, systemMessage, history);
    }
}
