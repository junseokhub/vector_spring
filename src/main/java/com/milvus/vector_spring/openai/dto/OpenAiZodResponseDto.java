package com.milvus.vector_spring.openai.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public record OpenAiZodResponseDto(
        List<ZodResponse> res,
        OpenAiUsageResponseDto usage
) {
    public record ZodResponse(
            String title,
            String answer
    ) {}

    public static OpenAiZodResponseDto of(JsonNode rootNode, ObjectMapper objectMapper) {
        try {
            String content = rootNode.path("choices").path(0).path("message").path("content").asText();

            JsonNode contentJson = objectMapper.readTree(content);
            List<ZodResponse> res = objectMapper.convertValue(
                    contentJson.path("res"),
                    new TypeReference<List<ZodResponse>>() {}
            );

            OpenAiUsageResponseDto usage = objectMapper.convertValue(
                    rootNode.path("usage"),
                    OpenAiUsageResponseDto.class
            );

            return new OpenAiZodResponseDto(res, usage);
        } catch (Exception e) {
            return new OpenAiZodResponseDto(List.of(), null);
        }
    }
}