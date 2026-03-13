package com.milvus.vector_spring.invite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMasterUserRequestDto {

    @NotBlank(message = "프로젝트 키는 필수입니다.")
    private String projectKey;

    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long createdUserId;

    @NotBlank(message = "변경할 마스터 이메일은 필수입니다.")
    private String changeMasterUser;

    @Builder
    private UpdateMasterUserRequestDto(String projectKey, Long createdUserId, String changeMasterUser) {
        this.projectKey = projectKey;
        this.createdUserId = createdUserId;
        this.changeMasterUser = changeMasterUser;
    }
}