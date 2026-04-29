package com.milvus.vector_spring.content.dto;

import com.milvus.vector_spring.content.Content;

import java.time.LocalDateTime;
import java.util.List;

public record ContentResponseDto(
        Long id,
        String key,
        String title,
        String answer,
        Long projectId,
        Long createdUserId,
        Long updatedUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
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

    public static List<ContentResponseDto> from(List<Content> contents) {
        return contents.stream().map(ContentResponseDto::from).toList();
    }
}
