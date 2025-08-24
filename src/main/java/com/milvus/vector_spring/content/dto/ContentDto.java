package com.milvus.vector_spring.content.dto;

import com.milvus.vector_spring.content.Content;
import lombok.Getter;

@Getter
public class ContentDto {
    private String title;

    public ContentDto(Content content) {
        this.title = content.getTitle();
    }
}
