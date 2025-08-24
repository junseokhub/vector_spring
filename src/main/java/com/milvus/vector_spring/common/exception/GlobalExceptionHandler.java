package com.milvus.vector_spring.common.exception;

import com.milvus.vector_spring.common.apipayload.BaseCode;
import com.milvus.vector_spring.common.apipayload.dto.ErrorResponseDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException e) {
        return failResponse(HttpStatus.FORBIDDEN, "You do not have access permission.");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException e) {
        BaseCode errorCode = e.getBaseCode();
        ErrorResponseDto response = errorCode.getReasonHttpStatus();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleBindException(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return failResponse(HttpStatus.BAD_REQUEST, errorMessages);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String msg = "HTTP method not supported: " + e.getMethod();
        return failResponse(HttpStatus.METHOD_NOT_ALLOWED, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = "Missing required parameter: " + e.getParameterName();
        return failResponse(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred";
        return failResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    private ResponseEntity<ErrorResponseDto> failResponse(HttpStatus status, String message) {
        ErrorResponseDto response = ErrorResponseDto.builder()
                .statusCode(String.valueOf(status.value()))
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
