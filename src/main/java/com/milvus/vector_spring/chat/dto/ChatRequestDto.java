package com.milvus.vector_spring.chat.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRequestDto {
    private String text;
    private String projectKey;
    private Long userId;

    @Builder
    public ChatRequestDto(String text, String projectKey, Long userId) {
        this.text = text;
        this.projectKey = projectKey;
        this.userId = userId;
    }
}
