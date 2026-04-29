package com.milvus.vector_spring.common.apipayload.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponseDto(
        String statusCode,
        String message,
        HttpStatus httpStatus
) {
    public static ErrorResponseDto of(String statusCode, String message, HttpStatus httpStatus) {
        return new ErrorResponseDto(statusCode, message, httpStatus);
    }

    public static ErrorResponseDto of(String statusCode, String message) {
        return new ErrorResponseDto(statusCode, message, null);
    }
}
