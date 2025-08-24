package com.milvus.vector_spring.user.dto;

import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.dto.ProjectResponseDto;
import com.milvus.vector_spring.user.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class UserProjectsResponseDto {
    private final Long id;
    private final String email;
    private final String username;
    private final LocalDateTime loginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ProjectResponseDto> projects;

    public UserProjectsResponseDto(User user, List<Project> projects) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.loginAt = user.getLoginAt();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.projects = projects.stream()
                .map(ProjectResponseDto::projectResponseDto)
                .toList();
    }

    public static UserProjectsResponseDto of(User user, List<Project> projects) {
        return new UserProjectsResponseDto(user, projects);
    }
}