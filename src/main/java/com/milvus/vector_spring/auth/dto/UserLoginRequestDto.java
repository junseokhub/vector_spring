package com.milvus.vector_spring.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginRequestDto {
    @Email(message = "이메일 형식 이어야 한다.")
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public UserLoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
