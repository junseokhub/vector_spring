package com.milvus.vector_spring.project.dto;

public record ProjectDeleteRequestDto(
        String key,
        Long userId
) {}
