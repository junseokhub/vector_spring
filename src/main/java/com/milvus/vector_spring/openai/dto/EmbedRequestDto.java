package com.milvus.vector_spring.openai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
public class EmbedRequestDto {
    @NotNull
    private String embedText;

    private int dimension;

    private String embedModel;

    @Builder
    public EmbedRequestDto(String embedText, int dimension, String embedModel) {
        this.embedText = embedText;
        this.dimension = dimension;
        this.embedModel = embedModel;
    }
}
