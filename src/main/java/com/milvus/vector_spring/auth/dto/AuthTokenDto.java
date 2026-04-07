package com.milvus.vector_spring.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokenDto {
    private final UserLoginResponseDto userInfo;
    private final String refreshToken;
}