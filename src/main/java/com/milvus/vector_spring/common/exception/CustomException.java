package com.milvus.vector_spring.common.exception;

import com.milvus.vector_spring.common.apipayload.BaseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final BaseCode baseCode;

    // 기본 생성자 (BaseCode만)
    public CustomException(BaseCode baseCode) {
        super(baseCode.getReasonHttpStatus().getMessage());
        this.baseCode = baseCode;
    }

    // BaseCode + 원인 포함
    public CustomException(BaseCode baseCode, Throwable cause) {
        super(baseCode.getReasonHttpStatus().getMessage(), cause);
        this.baseCode = baseCode;
    }

    // BaseCode + 커스텀 메시지
    public CustomException(BaseCode baseCode, String customMessage) {
        super(customMessage);
        this.baseCode = baseCode;
    }

    // BaseCode + 커스텀 메시지 + 원인 포함
    public CustomException(BaseCode baseCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.baseCode = baseCode;
    }
}
