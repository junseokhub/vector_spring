package com.milvus.vector_spring.user.dto;

import com.milvus.vector_spring.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    private final Long id;
    private final String email;
    private final String username;
    private final String role;
    private final LocalDateTime loginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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