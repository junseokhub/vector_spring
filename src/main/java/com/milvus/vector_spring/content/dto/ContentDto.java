package com.milvus.vector_spring.content.dto;

import com.milvus.vector_spring.content.Content;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentDto {
    private Long id;
    private String key;
    private String title;
    private String answer;

    @Builder
    private ContentDto(Long id, String key, String title, String answer) {
        this.id = id;
        this.key = key;
        this.title = title;
        this.answer = answer;
    }

    public ContentDto(Content content) {
        if (content != null) {
            this.id = content.getId();
            this.key = content.getKey();
            this.title = content.getTitle();
            this.answer = content.getAnswer();
        }
    }
}