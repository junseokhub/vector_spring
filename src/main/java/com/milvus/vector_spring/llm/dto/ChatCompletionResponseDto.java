package com.milvus.vector_spring.llm.dto;

public record ChatCompletionResponseDto(
        String content,
        long totalTokens
) {}
