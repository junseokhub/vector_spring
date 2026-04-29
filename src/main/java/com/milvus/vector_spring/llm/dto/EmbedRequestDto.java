package com.milvus.vector_spring.llm.dto;

import com.milvus.vector_spring.llm.LlmPlatform;

public record EmbedRequestDto(
        LlmPlatform platform,
        String apiKey,
        String model,
        String input,
        long dimensions
) {
    public static EmbedRequestDto from(LlmPlatform platform, String apiKey, String model, String input, long dimensions) {
        return new EmbedRequestDto(platform, apiKey, model, input, dimensions);
    }
}
