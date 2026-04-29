package com.milvus.vector_spring.auth.dto;

public record AuthTokenDto(
        UserLoginResponseDto userInfo,
        String refreshToken
) {}
