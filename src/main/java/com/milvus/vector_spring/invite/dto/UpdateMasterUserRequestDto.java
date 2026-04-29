package com.milvus.vector_spring.invite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateMasterUserRequestDto(
        @NotBlank(message = "프로젝트 키는 필수입니다.")
        String projectKey,

        @NotNull(message = "작성자 ID는 필수입니다.")
        Long createdUserId,

        @NotBlank(message = "변경할 마스터 이메일은 필수입니다.")
        String changeMasterUser
) {}
