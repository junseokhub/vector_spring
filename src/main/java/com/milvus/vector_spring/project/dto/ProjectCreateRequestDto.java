package com.milvus.vector_spring.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectCreateRequestDto(
        @NotBlank(message = "필수 입력")
        String name,

        long dimensions,

        @NotNull
        Long createdUserId
) {}
