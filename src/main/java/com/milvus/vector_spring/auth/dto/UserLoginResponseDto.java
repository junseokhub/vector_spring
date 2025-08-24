package com.milvus.vector_spring.auth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginResponseDto {
    private Long id;
    private String email;
    private String username;
    private String role;
    private String accessToken;
    private LocalDateTime loginAt;

    @Builder
    public UserLoginResponseDto(Long id, String email, String username, String role, String accessToken, LocalDateTime loginAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.accessToken = accessToken;
        this.loginAt = loginAt;
    }
}
