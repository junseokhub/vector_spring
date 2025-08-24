package com.milvus.vector_spring.user.dto;

import com.milvus.vector_spring.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {

    private final Long id;
    private final String email;
    private final String username;
    private final String role;
    private final LocalDateTime loginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserResponseDto(Long id, String email, String username, String role,
                           LocalDateTime loginAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.loginAt = loginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
