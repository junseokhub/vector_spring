package com.milvus.vector_spring.content.dto;

import jakarta.validation.constraints.NotNull;

public record ContentUpdateRequestDto(
        String title,
        String answer,

        @NotNull(message = "필수 입력")
        Long updatedUserId
) {}
