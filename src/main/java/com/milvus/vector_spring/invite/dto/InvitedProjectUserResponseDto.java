package com.milvus.vector_spring.invite.dto;

import com.milvus.vector_spring.invite.Invite;
import lombok.Getter;
import java.util.List;

@Getter
public class InvitedProjectUserResponseDto {
    private final String projectKey;
    private final Long createdUserId;
    private final List<String> receivedEmail;

    private InvitedProjectUserResponseDto(String projectKey, Long createdUserId, List<String> receivedEmail) {
        this.projectKey = projectKey;
        this.createdUserId = createdUserId;
        this.receivedEmail = receivedEmail;
    }

    public static InvitedProjectUserResponseDto from(String projectKey, List<Invite> invitedList) {
        return new InvitedProjectUserResponseDto(
                projectKey,
                invitedList.get(0).getCreatedBy().getId(),
                invitedList.stream().map(Invite::getReceivedEmail).toList()
        );
    }
}