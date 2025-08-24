package com.milvus.vector_spring.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpRequestDto {
    @NotBlank
    @NotNull
    private String username;

    @NotBlank
    @NotNull
    @Email(message = "이메일 형식 이어야 한다.")
    private String email;

    @NotBlank
    @NotNull
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
        message = "비밀번호는 숫자, 문자, 특수문자 1개 이상 ")
    private String password;

    public UserSignUpRequestDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
