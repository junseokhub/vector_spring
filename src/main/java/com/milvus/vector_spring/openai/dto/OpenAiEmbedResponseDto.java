package com.milvus.vector_spring.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiEmbedResponseDto(
        String object,
        String model,
        List<Data> data,
        OpenAiUsageResponseDto usage
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String object,
            int index,
            List<Float> embedding
    ) {}
}