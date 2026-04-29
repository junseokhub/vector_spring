package com.milvus.vector_spring.llm;

/**
 * @param dimensions    null for CHAT models; the fixed or maximum dimension for EMBED models
 * @param flexDimensions true if the model accepts custom dimensions (1..dimensions); false = exact match required
 */
public record LlmModelInfo(
        LlmPlatform platform,
        ModelType type,
        String name,
        Long dimensions,
        boolean flexDimensions
) {}
