package com.milvus.vector_spring.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {
    private String username;

    @Email(message = "이메일 형식 이어야 한다.")
    private String email;

    @Builder
    public UserUpdateRequestDto(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
