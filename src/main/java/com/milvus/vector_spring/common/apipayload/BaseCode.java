package com.milvus.vector_spring.common.apipayload;

import com.milvus.vector_spring.common.apipayload.dto.ErrorResponseDto;

public interface BaseCode {
    public ErrorResponseDto getReasonHttpStatus();
}