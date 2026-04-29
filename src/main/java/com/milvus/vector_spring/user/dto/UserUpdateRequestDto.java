package com.milvus.vector_spring.user.dto;

import jakarta.validation.constraints.Email;

public record UserUpdateRequestDto(
        String username,

        @Email(message = "이메일 형식 이어야 한다.")
        String email,

        String password
) {}
