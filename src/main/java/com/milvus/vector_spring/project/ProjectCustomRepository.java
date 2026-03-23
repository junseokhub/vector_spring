package com.milvus.vector_spring.project;


import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;

import java.util.List;

public interface ProjectCustomRepository {
    Project findOneProjectWithContents(String projectKey);

    List<CombinedProjectListResponseDto> findMyProjectsAsDto(Long userId);
}
