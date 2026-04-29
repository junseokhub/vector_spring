package com.milvus.vector_spring.invite.dto;

public record BanishUserRequestDto(
        String masterUserEmail,
        String banishedEmail,
        String projectKey
) {}
