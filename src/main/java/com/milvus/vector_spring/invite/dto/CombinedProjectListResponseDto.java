package com.milvus.vector_spring.invite.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CombinedProjectListResponseDto {
    private Long id;
    private boolean mine;
    private String name;
    private String key;
    private Long createdUserId;
    private Long updatedUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    @QueryProjection
    public CombinedProjectListResponseDto(Long id, boolean mine, String name, String key,
                                          Long createdUserId, Long updatedUserId,
                                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.mine = mine;
        this.name = name;
        this.key = key;
        this.createdUserId = createdUserId;
        this.updatedUserId = updatedUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}