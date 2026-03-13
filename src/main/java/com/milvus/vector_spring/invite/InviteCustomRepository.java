package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;

import java.util.List;

public interface InviteCustomRepository {
    List<CombinedProjectListResponseDto> findInvitedProjectsAsDto(String email);
}
