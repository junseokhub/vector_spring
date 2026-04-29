package com.milvus.vector_spring.content.dto;

import jakarta.validation.constraints.NotBlank;

public record ContentCreateRequestDto(
        @NotBlank(message = "필수 입력")
        String title,

        String answer,

        @NotBlank(message = "필수 입력")
        String projectKey
) {}
