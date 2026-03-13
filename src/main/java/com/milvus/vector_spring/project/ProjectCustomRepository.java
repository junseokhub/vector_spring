package com.milvus.vector_spring.project;


import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;

import java.util.List;
import java.util.Optional;

public interface ProjectCustomRepository {
    Optional<Project> findOneProjectWithContents(String projectKey);

    List<CombinedProjectListResponseDto> findMyProjectsAsDto(Long userId);
}
