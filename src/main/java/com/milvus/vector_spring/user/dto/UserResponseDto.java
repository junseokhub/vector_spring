package com.milvus.vector_spring.user.dto;

import com.milvus.vector_spring.user.User;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String email,
        String username,
        String role,
        LocalDateTime loginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
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
