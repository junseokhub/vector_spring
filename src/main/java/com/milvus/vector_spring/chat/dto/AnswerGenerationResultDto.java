package com.milvus.vector_spring.chat.dto;

import java.time.LocalDateTime;

public record AnswerGenerationResultDto(
        String finalAnswer,
        long totalToken,
        LocalDateTime outputDateTime,
        boolean isPromptAnswer
) {}