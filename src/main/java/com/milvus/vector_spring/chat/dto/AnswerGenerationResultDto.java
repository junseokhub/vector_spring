package com.milvus.vector_spring.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AnswerGenerationResultDto {
    private String finalAnswer;
    private long totalToken;
}