package com.milvus.vector_spring.project.dto;

import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.content.dto.ContentResponseDto;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProjectContentsResponseDto {
    private final Long id;
    private final String name;
    private final String key;
    private final String apiKey;
    private final String prompt;
    private final String embedModel;
    private final String chatModel;
    private final long dimensions;
    private final LlmPlatform llmPlatform;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ContentResponseDto> contents;

    public static ProjectContentsResponseDto from(Project project, List<Content> contents) {
        return new ProjectContentsResponseDto(
                project.getId(),
                project.getName(),
                project.getKey(),
                project.getApiKey(),
                project.getPrompt(),
                project.getEmbedModel(),
                project.getChatModel(),
                project.getDimensions(),
                project.getLlmPlatform(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                contents.stream()
                        .map(ContentResponseDto::from)
                        .toList()
        );
    }
}
