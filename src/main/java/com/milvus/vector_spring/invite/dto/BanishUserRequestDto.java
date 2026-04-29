package com.milvus.vector_spring.invite.dto;

public record BanishUserRequestDto(
        String banishedEmail,
        String projectKey
) {}
