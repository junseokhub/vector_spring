package com.milvus.vector_spring.auth.dto;

import java.time.LocalDateTime;

public record UserLoginCheckResponseDto(
        Long id,
        String email,
        String accessToken,
        LocalDateTime loginAt
) {}
