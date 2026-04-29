package com.milvus.vector_spring.llm.dto;

import com.milvus.vector_spring.llm.LlmModelInfo;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.ModelType;

public record ModelListResponseDto(
        LlmPlatform platform,
        ModelType type,
        String name,
        Long dimensions,
        boolean flexDimensions
) {
    public static ModelListResponseDto from(LlmModelInfo info) {
        return new ModelListResponseDto(
                info.platform(),
                info.type(),
                info.name(),
                info.dimensions(),
                info.flexDimensions()
        );
    }
}
