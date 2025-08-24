package com.milvus.vector_spring.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectCreateRequestDto {
    @NotNull(message = "필수 입력")
    @NotBlank
    private String name;

    @NotNull
    private long dimensions;

    @NotNull
    private Long createdUserId;

    @Builder
    public ProjectCreateRequestDto(String name, long dimensions, Long createdUserId) {
        this.name = name;
        this.dimensions = dimensions;
        this.createdUserId = createdUserId;
    }
}
