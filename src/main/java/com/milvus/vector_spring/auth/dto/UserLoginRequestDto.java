package com.milvus.vector_spring.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @Email(message = "이메일 형식 이어야 한다.")
        @NotBlank
        String email,

        @NotBlank
        String password
) {}
