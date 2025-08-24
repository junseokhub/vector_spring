package com.milvus.vector_spring.content.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentUpdateRequestDto {
    private String title;
    private String answer;

    @NotNull(message = "필수 입력")
    private Long updatedUserId;

    @Builder
    public ContentUpdateRequestDto(String title, String answer, Long updatedUserId) {
        this.title = title;
        this.answer = answer;
        this.updatedUserId = updatedUserId;
    }
}
