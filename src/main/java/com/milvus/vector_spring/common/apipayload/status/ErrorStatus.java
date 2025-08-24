package com.milvus.vector_spring.common.apipayload.status;

import com.milvus.vector_spring.common.apipayload.BaseCode;
import com.milvus.vector_spring.common.apipayload.dto.ErrorResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error. Send Mail"),
    OPEN_AI_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Open AI Error."),
    MILVUS_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Milvus Error."),
    EMBEDDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create embedding."),
    MILVUS_SCHEMA_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Schema Create Error."),
    MILVUS_INDEX_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Index Create Error."),
    MILVUS_UPSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Upsert Error."),
    MILVUS_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Delete Error."),
    MILVUS_VECTOR_SEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Vector Search Error."),
    OPENAI_EMBEDDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAi Embedding Error."),
    DATA_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Data Parse Error."),

    // 400 Bad Request
    DUPLICATE_USER_EMAIL(HttpStatus.BAD_REQUEST, "Duplicate User Email."),
    NOT_PASSWORD_MATCHES(HttpStatus.BAD_REQUEST, "Different Password."),
    NOT_INVITED_USER(HttpStatus.BAD_REQUEST, "Not Invited."),
    REQUIRE_OPEN_AI_KEY(HttpStatus.BAD_REQUEST, "Not Found Open AI Key."),
    REQUIRE_OPEN_AI_INFO(HttpStatus.BAD_REQUEST, "Open AI Info Error."),
    EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "Email not registered."),
    UNKNOWING_MODEL(HttpStatus.BAD_REQUEST, "Unknown GPT Model."),
    DECRYPTION_ERROR(HttpStatus.BAD_REQUEST, "Decryption Error."),
    ENCRYPTION_ERROR(HttpStatus.BAD_REQUEST, "Encryption Error."),

    // 401 Unauthorized
    OPEN_AI_KEY_ERROR(HttpStatus.UNAUTHORIZED, "Invalid OpenAi Key."),
    COOKIE_ENCODING_ERROR(HttpStatus.UNAUTHORIZED, "Cookie Encoding Error."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Access Token."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Token is required"),
    INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "Invalid token format"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Expired refresh token"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid password."),
    NOT_PROJECT_MASTER_USER(HttpStatus.UNAUTHORIZED, "Not Project Master User."),

    // 404 Not Found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "User Not Found."),
    NOT_FOUND_PROJECT(HttpStatus.NOT_FOUND, "Not Found Project."),
    NOT_FOUND_CONTENT(HttpStatus.NOT_FOUND, "Not Found Content."),

    // 409 Conflict
    MILVUS_COLLECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Collection Already Exists.");


    private final HttpStatus httpStatus;
    private final String message;

    public String getStatusCode() {
        return String.valueOf(httpStatus.value());
    }

    @Override
    public ErrorResponseDto getReasonHttpStatus() {
        return ErrorResponseDto.builder()
                .statusCode(getStatusCode())
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}
