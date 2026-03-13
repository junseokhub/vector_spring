package com.milvus.vector_spring.invite.dto;

import com.milvus.vector_spring.invite.Invite;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InviteResponseDto {
    private final Long id;
    private final String receivedEmail;
    private final String projectKey;
    private final Long invitedId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public InviteResponseDto(Invite invite) {
        this.id = invite.getId();
        this.receivedEmail = invite.getReceivedEmail();
        this.invitedId = invite.getCreatedBy().getId();
        this.projectKey = invite.getProject().getKey();
        this.createdAt = invite.getCreatedAt();
        this.updatedAt = invite.getUpdatedAt();
    }
}
