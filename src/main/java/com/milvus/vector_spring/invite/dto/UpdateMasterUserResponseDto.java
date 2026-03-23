package com.milvus.vector_spring.invite.dto;

public record UpdateMasterUserResponseDto(
        String projectKey,
        String beforeMasterEmail,
        String afterMasterEmail
) {
}