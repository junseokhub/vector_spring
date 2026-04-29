package com.milvus.vector_spring.invite.dto;

import com.milvus.vector_spring.invite.ProjectMember;

import java.util.List;

public record InvitedProjectUserResponseDto(
        String projectKey,
        Long ownerId,
        List<MemberInfo> members
) {
    public record MemberInfo(Long userId, String email) {}

    public static InvitedProjectUserResponseDto from(String projectKey, List<ProjectMember> members) {
        Long ownerId = members.get(0).getInvitedBy().getId();
        List<MemberInfo> memberInfos = members.stream()
                .map(m -> new MemberInfo(
                        m.getMember() != null ? m.getMember().getId() : null,
                        m.getMemberEmail()
                ))
                .toList();
        return new InvitedProjectUserResponseDto(projectKey, ownerId, memberInfos);
    }
}
