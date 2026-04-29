package com.milvus.vector_spring.project.dto;

import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.project.Project;

import java.time.LocalDateTime;

public record ProjectResponseDto(
        Long id,
        String name,
        String key,
        String prompt,
        String embedModel,
        String chatModel,
        long dimensions,
        LlmPlatform llmPlatform,
        Long createdUserId,
        Long updatedUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponseDto from(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getKey(),
                project.getPrompt(),
                project.getEmbedModel(),
                project.getChatModel(),
                project.getDimensions(),
                project.getLlmPlatform(),
                project.getCreatedBy().getId(),
                project.getUpdatedBy().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
