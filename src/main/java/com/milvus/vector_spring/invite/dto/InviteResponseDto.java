package com.milvus.vector_spring.invite.dto;

import com.milvus.vector_spring.invite.ProjectMember;

import java.time.LocalDateTime;

public record InviteResponseDto(
        Long id,
        Long memberId,
        String memberEmail,
        String projectKey,
        Long invitedById,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InviteResponseDto from(ProjectMember member) {
        return new InviteResponseDto(
                member.getId(),
                member.getMember() != null ? member.getMember().getId() : null,
                member.getMemberEmail(),
                member.getProject().getKey(),
                member.getInvitedBy().getId(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
