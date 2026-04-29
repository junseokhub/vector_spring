package com.milvus.vector_spring.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserSignUpRequestDto(
        @NotBlank
        String username,

        @NotBlank
        @Email(message = "이메일 형식 이어야 한다.")
        String email,

        @NotBlank
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
            message = "비밀번호는 숫자, 문자, 특수문자 1개 이상")
        String password
) {}
