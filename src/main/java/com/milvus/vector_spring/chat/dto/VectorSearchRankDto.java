package com.milvus.vector_spring.chat.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VectorSearchRankDto {
    private String answer;
    private String title;
    private double score;
    private Long id;

    @Builder
    public VectorSearchRankDto(String answer, String title, double score, Long id) {
        this.answer = answer;
        this.title = title;
        this.score = score;
        this.id = id;
    }
}
