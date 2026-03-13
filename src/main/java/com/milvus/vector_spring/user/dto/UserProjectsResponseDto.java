package com.milvus.vector_spring.user.dto;

import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.dto.ProjectResponseDto;
import com.milvus.vector_spring.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserProjectsResponseDto {
    private final Long id;
    private final String email;
    private final String username;
    private final LocalDateTime loginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ProjectResponseDto> projects;

    public static UserProjectsResponseDto from(User user, List<Project> projects) {
        return new UserProjectsResponseDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                projects.stream()
                        .map(ProjectResponseDto::from)
                        .toList()
        );
    }
}