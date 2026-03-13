package com.milvus.vector_spring.openai.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAiChatRequestDto {
    private String model;
    private List<OpenAiMessageDto> messages;

    @Builder
    public OpenAiChatRequestDto(String model, List<OpenAiMessageDto> messages) {
        this.model = model;
        this.messages = messages;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OpenAiMessageDto {
        private String role;
        private String content;

        // private -> public으로 변경
        @Builder
        public OpenAiMessageDto(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}