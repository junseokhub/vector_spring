package com.milvus.vector_spring.chat.dto;

public record ChatRequestDto(
        String projectKey,
        String sessionId,
        String text
) {}