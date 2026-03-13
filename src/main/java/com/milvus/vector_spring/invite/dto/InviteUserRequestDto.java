package com.milvus.vector_spring.invite.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InviteUserRequestDto {

    @NotNull(message = "초대자 ID는 필수입니다.")
    private Long inviteId;

    @NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String receiveEmail;

    @NotBlank(message = "프로젝트 키는 필수입니다.")
    private String projectKey;

    @Builder
    private InviteUserRequestDto(Long inviteId, String receiveEmail, String projectKey) {
        this.inviteId = inviteId;
        this.receiveEmail = receiveEmail;
        this.projectKey = projectKey;
    }
}