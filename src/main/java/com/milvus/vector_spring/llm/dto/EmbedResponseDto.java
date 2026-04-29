package com.milvus.vector_spring.llm.dto;

import java.util.List;

public record EmbedResponseDto(
        List<Float> embedding,
        long totalTokens
) {}