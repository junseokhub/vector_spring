package com.milvus.vector_spring.project.dto;

import com.milvus.vector_spring.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponseDto {

    private final Long id;
    private final String name;
    private final String key;
    private final String prompt;
    private final String embedModel;
    private final String chatModel;
    private final long dimensions;
    private final Long createdUserId;
    private final Long updatedUserId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ProjectResponseDto from(Project project) {
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