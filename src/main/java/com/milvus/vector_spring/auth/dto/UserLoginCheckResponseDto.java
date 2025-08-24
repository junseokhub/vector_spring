package com.milvus.vector_spring.auth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginCheckResponseDto {
    private Long id;
    private String email;
    private String accessToken;
    private LocalDateTime loginAt;

    @Builder
    public UserLoginCheckResponseDto(Long id, String email, String accessToken, LocalDateTime loginAt) {
        this.id = id;
        this.email = email;
        this.accessToken = accessToken;
        this.loginAt = loginAt;
    }
}
