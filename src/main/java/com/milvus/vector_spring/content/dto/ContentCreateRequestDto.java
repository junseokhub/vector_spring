package com.milvus.vector_spring.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentCreateRequestDto {
    @NotNull(message = "필수 입력")
    @NotBlank
    private String title;

    private String answer;


    @NotBlank
    @NotNull(message = "필수 입력")
    private String projectKey;

    @Builder
    public ContentCreateRequestDto(String title, String answer, String projectKey) {
        this.title = title;
        this.answer = answer;
        this.projectKey = projectKey;
    }
}
