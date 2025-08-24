package com.milvus.vector_spring.common.apipayload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private String statusCode;
    private String message;
    private HttpStatus httpStatus;
}