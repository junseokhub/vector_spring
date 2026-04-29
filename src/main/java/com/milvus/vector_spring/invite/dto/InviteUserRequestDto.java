package com.milvus.vector_spring.invite.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteUserRequestDto(
        @NotNull(message = "초대자 ID는 필수입니다.")
        Long inviteId,

        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String receiveEmail,

        @NotBlank(message = "프로젝트 키는 필수입니다.")
        String projectKey
) {}
