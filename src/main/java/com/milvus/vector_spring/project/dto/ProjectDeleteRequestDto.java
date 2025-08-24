package com.milvus.vector_spring.project.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectDeleteRequestDto {
    private String key;
    private Long userId;

    @Builder
    public ProjectDeleteRequestDto(String key, Long userId) {
        this.key = key;
        this.userId = userId;
    }
}
