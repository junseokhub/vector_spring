package com.milvus.vector_spring.invite.dto;

import com.milvus.vector_spring.project.Project;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CombinedProjectListResponseDto {
    private final Long id;
    private final boolean mine;
    private final String name;
    private final String key;
    private final Long createdUserId;
    private final Long updatedUserId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @QueryProjection
    public CombinedProjectListResponseDto(Project project, boolean mine) {
        this.id = project.getId();
        this.mine = mine;
        this.name = project.getName();
        this.key = project.getKey();
        this.createdUserId = (project.getCreatedBy() != null) ? project.getCreatedBy().getId() : null;
        this.updatedUserId = (project.getUpdatedBy() != null) ? project.getUpdatedBy().getId() : null;
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
    }

    public static CombinedProjectListResponseDto from(Project project, boolean mine) {
        return new CombinedProjectListResponseDto(project, mine);
    }
}