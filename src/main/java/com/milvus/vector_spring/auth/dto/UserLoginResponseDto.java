package com.milvus.vector_spring.auth.dto;

import java.time.LocalDateTime;

public record UserLoginResponseDto(
        Long id,
        String email,
        String username,
        String role,
        String accessToken,
        LocalDateTime loginAt
) {}
