package com.milvus.vector_spring.invite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BanishUserRequestDto {
    private String masterUserEmail;
    private String banishedEmail;
    private String projectKey;
}
