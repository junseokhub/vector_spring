package com.milvus.vector_spring.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiZodResponseDto {

    private final List<ZodResponse> res;
    private final OpenAiUsageResponseDto usage;

    public OpenAiZodResponseDto(List<ZodResponse> res, OpenAiUsageResponseDto usage) {
        this.res = res;
        this.usage = usage;
    }

    @Getter
    public static class ZodResponse {
        private final String title;
        private final String answer;

        public ZodResponse(@JsonProperty("title") String title, @JsonProperty("answer") String answer) {
            this.title = title;
            this.answer = answer;
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static OpenAiZodResponseDto fromJson(JsonNode jsonNode) {
        JsonNode contentNode = jsonNode.path("choices").path(0).path("message").path("content");

        if (contentNode.isTextual()) {
            try {
                JsonNode contentJson = objectMapper.readTree(contentNode.asText());
                List<ZodResponse> res = objectMapper.convertValue(
                        contentJson.get("res"),
                        new TypeReference<List<ZodResponse>>() {}
                );
                OpenAiUsageResponseDto usage = objectMapper.convertValue(jsonNode.get("usage"), OpenAiUsageResponseDto.class);
                return new OpenAiZodResponseDto(res, usage);
            } catch (Exception e) {
                return new OpenAiZodResponseDto(List.of(), null);
            }
        }
        return new OpenAiZodResponseDto(List.of(), null);
    }
}
