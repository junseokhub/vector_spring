package com.milvus.vector_spring.common.apipayload.status;

import com.milvus.vector_spring.common.apipayload.BaseCode;
import com.milvus.vector_spring.common.apipayload.dto.ErrorResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error."),
    OPEN_AI_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "LLM provider error."),
    MILVUS_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Milvus error."),
    EMBEDDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create embedding."),
    MILVUS_SCHEMA_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Schema creation failed."),
    MILVUS_INDEX_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Index creation failed."),
    MILVUS_UPSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Upsert failed."),
    MILVUS_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Collection deletion failed."),
    MILVUS_VECTOR_SEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Vector search failed."),
    OPENAI_EMBEDDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Embedding request failed."),
    DATA_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Data parsing failed."),
    CHAT_TIMEOUT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Chat request timed out."),

    // 400 Bad Request
    DUPLICATE_USER_EMAIL(HttpStatus.BAD_REQUEST, "Email already registered."),
    NOT_PASSWORD_MATCHES(HttpStatus.BAD_REQUEST, "Incorrect password."),
    NOT_INVITED_USER(HttpStatus.BAD_REQUEST, "User is not a project member."),
    REQUIRE_OPEN_AI_KEY(HttpStatus.BAD_REQUEST, "OpenAI key not configured."),
    REQUIRE_OPEN_AI_INFO(HttpStatus.BAD_REQUEST, "LLM configuration is incomplete."),
    EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "Email not registered."),
    UNKNOWING_MODEL(HttpStatus.BAD_REQUEST, "Unsupported LLM model."),
    INVALID_MODEL(HttpStatus.BAD_REQUEST, "Model not found in registry."),
    DIMENSION_MISMATCH(HttpStatus.BAD_REQUEST, "Embedding dimensions do not match the selected model."),
    DECRYPTION_ERROR(HttpStatus.BAD_REQUEST, "Decryption failed."),
    ENCRYPTION_ERROR(HttpStatus.BAD_REQUEST, "Encryption failed."),

    // 401 Unauthorized
    OPEN_AI_KEY_ERROR(HttpStatus.UNAUTHORIZED, "Invalid OpenAI key."),
    COOKIE_ENCODING_ERROR(HttpStatus.UNAUTHORIZED, "Cookie encoding error."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid access token."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Token is required."),
    INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "Invalid token format."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh token expired."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid password."),
    NOT_PROJECT_MASTER_USER(HttpStatus.UNAUTHORIZED, "Not the project owner."),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied."),

    // 404 Not Found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "User not found."),
    NOT_FOUND_PROJECT(HttpStatus.NOT_FOUND, "Project not found."),
    NOT_FOUND_CONTENT(HttpStatus.NOT_FOUND, "Content not found."),

    // 409 Conflict
    MILVUS_COLLECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Collection already exists."),

    // 429 Too Many Requests
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public ErrorResponseDto getReasonHttpStatus() {
        return ErrorResponseDto.of(String.valueOf(httpStatus.value()), message, httpStatus);
    }
}
