package com.milvus.vector_spring.common.exception;

import com.milvus.vector_spring.common.apipayload.BaseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final BaseCode baseCode;

    public CustomException(BaseCode baseCode) {
        super(baseCode.getReasonHttpStatus().message());
        this.baseCode = baseCode;
    }

    public CustomException(BaseCode baseCode, Throwable cause) {
        super(baseCode.getReasonHttpStatus().message(), cause);
        this.baseCode = baseCode;
    }

    public CustomException(BaseCode baseCode, String customMessage) {
        super(customMessage);
        this.baseCode = baseCode;
    }
}
