package com.milvus.vector_spring.project.dto;

import com.milvus.vector_spring.llm.LlmPlatform;
import jakarta.validation.constraints.NotNull;

public record ProjectUpdateRequestDto(
        String name,
        String apiKey,
        String prompt,
        String embedModel,
        String chatModel,
        long dimensions,
        LlmPlatform llmPlatform,

        @NotNull
        Long updatedUserId
) {}
