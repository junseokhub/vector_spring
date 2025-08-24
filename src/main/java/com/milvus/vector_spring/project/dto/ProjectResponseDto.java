package com.milvus.vector_spring.project.dto;

import com.milvus.vector_spring.project.Project;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectResponseDto {

    private final Long id;
    private final String name;
    private final String key;
    private String prompt;
    private String embedModel;
    private String chatModel;
    private long dimensions;
    private final Long createdUserId;
    private final Long updatedUserId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ProjectResponseDto(
            Long id,
            String name,
            String key,
            String prompt,
            String embedModel,
            String chatModel,
            long dimensions,
            Long createdUserId,
            Long updatedUserId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.prompt = prompt;
        this.embedModel = embedModel;
        this.chatModel = chatModel;
        this.dimensions = dimensions;
        this.createdUserId = createdUserId;
        this.updatedUserId = updatedUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProjectResponseDto projectResponseDto(Project project) {
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getKey(),
                project.getPrompt(),
                project.getEmbedModel(),
                project.getChatModel(),
                project.getDimensions(),
                project.getCreatedBy().getId(),
                project.getUpdatedBy().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
