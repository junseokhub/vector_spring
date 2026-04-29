package com.milvus.vector_spring.chat.dto;

public record VectorSearchRankDto(
        Long id,
        String title,
        String answer,
        double score
) {}