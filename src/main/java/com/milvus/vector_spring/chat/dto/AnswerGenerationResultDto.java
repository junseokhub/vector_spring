package com.milvus.vector_spring.chat.dto;

import java.time.LocalDateTime;

public record AnswerGenerationResultDto(
        String finalAnswer,
        long totalToken,
        LocalDateTime outputDateTime,
        boolean isPromptAnswer
) {
    public static AnswerGenerationResultDto noMatch(long embeddingTokens) {
        return new AnswerGenerationResultDto(
                "No matching answer found.", embeddingTokens, LocalDateTime.now(), false
        );
    }

    public static AnswerGenerationResultDto fromVector(String answer, long embeddingTokens) {
        return new AnswerGenerationResultDto(answer, embeddingTokens, LocalDateTime.now(), false);
    }

    public static AnswerGenerationResultDto fromLlm(String answer, long totalTokens) {
        return new AnswerGenerationResultDto(answer, totalTokens, LocalDateTime.now(), true);
    }
}