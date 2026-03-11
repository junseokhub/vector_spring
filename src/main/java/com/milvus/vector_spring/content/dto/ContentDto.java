package com.milvus.vector_spring.content.dto;

import com.milvus.vector_spring.content.Content;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {
    private Long id;
    private String key;
    private String title;
    private String answer;

    public ContentDto(Content content) {
        if (content != null) {
            this.id = content.getId();
            this.key = content.getKey();
            this.title = content.getTitle();
            this.answer = content.getAnswer();
        } else {
            this.id = null;
            this.key = null;
            this.title = null;
            this.answer = null;
        }
    }
}