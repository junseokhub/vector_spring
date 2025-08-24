package com.milvus.vector_spring.content.dto;

import com.milvus.vector_spring.content.Content;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ContentResponseDto {
    private Long id;
    private String key;
    private String title;
    private String answer;
    private Long projectId;
    private Long createdUserId;
    private Long updatedUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContentResponseDto(Long id, String key, String title, String answer, Long projectId,
                              Long createdUserId, Long updatedUserId,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.key = key;
        this.title = title;
        this.answer = answer;
        this.projectId = projectId;
        this.createdUserId = createdUserId;
        this.updatedUserId = updatedUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ContentResponseDto from(Content content) {
        return new ContentResponseDto(
                content.getId(),
                content.getKey(),
                content.getTitle(),
                content.getAnswer(),
                content.getProject().getId(),
                content.getCreatedBy().getId(),
                content.getUpdatedBy().getId(),
                content.getCreatedAt(),
                content.getUpdatedAt()
        );
    }
}
