package com.milvus.vector_spring.project.dto;

import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.content.dto.ContentResponseDto;
import com.milvus.vector_spring.project.Project;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProjectContentsResponseDto {
    private Long id;
    private String name;
    private String key;
    private String openAiKey;
    private String prompt;
    private String embedModel;
    private String chatModel;
    private long dimensions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ContentResponseDto> contents;

    public ProjectContentsResponseDto(
            Long id,
            String name,
            String key,
            String openAiKey,
            String prompt,
            String embedModel,
            String chatModel,
            long dimensions,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<ContentResponseDto> contents
    ) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.openAiKey = openAiKey;
        this.prompt = prompt;
        this.embedModel = embedModel;
        this.chatModel = chatModel;
        this.dimensions = dimensions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.contents = contents;
    }

    public static ProjectContentsResponseDto projectContentsResponseDto(Project project, List<Content> contents) {
        return new ProjectContentsResponseDto(
                project.getId(),
                project.getName(),
                project.getKey(),
                project.getOpenAiKey(),
                project.getPrompt(),
                project.getEmbedModel(),
                project.getChatModel(),
                project.getDimensions(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                contents.stream()
                        .map(ContentResponseDto::from)
                        .collect(Collectors.toList())
        );
    }
}
