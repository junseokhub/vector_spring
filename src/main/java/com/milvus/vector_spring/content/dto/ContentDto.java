package com.milvus.vector_spring.content.dto;

import com.milvus.vector_spring.content.Content;
import lombok.Getter;

@Getter
public class ContentDto {
    private final Long id;
    private final String key;
    private final String title;
    private final String answer;

    public ContentDto(Content content) {
        this.id = content.getId();
        this.key = content.getKey();
        this.title = content.getTitle();
        this.answer = content.getAnswer();
    }
}