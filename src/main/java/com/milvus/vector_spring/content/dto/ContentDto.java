package com.milvus.vector_spring.content.dto;

public record ContentDto(
        Long id,
        String key,
        String title,
        String answer
) {
    public static ContentDto from(com.milvus.vector_spring.content.Content content) {
        return new ContentDto(content.getId(), content.getKey(), content.getTitle(), content.getAnswer());
    }
}