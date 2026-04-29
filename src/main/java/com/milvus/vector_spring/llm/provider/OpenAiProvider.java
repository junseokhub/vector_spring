package com.milvus.vector_spring.llm.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.properties.LlmProperties;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.dto.ChatCompletionRequestDto;
import com.milvus.vector_spring.llm.dto.ChatCompletionResponseDto;
import com.milvus.vector_spring.llm.dto.EmbedRequestDto;
import com.milvus.vector_spring.llm.dto.EmbedResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiProvider implements LlmProvider {

    private final WebClient webClient;

    public OpenAiProvider(LlmProperties llmProperties) {
        this.webClient = WebClient.builder()
                .baseUrl(llmProperties.openai().baseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public boolean supports(LlmPlatform platform) {
        return platform == LlmPlatform.OPENAI;
    }

    @Override
    public ChatCompletionResponseDto chat(ChatCompletionRequestDto request) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (request.systemMessage() != null && !request.systemMessage().isBlank()) {
                messages.add(Map.of("role", "system", "content", request.systemMessage()));
            }
            messages.add(Map.of("role", "user", "content", request.userMessage()));

            ChatCompletionApiResponse response = webClient.post()
                    .uri("/v1/chat/completions")
                    .header("Authorization", "Bearer " + request.apiKey())
                    .bodyValue(Map.of("model", request.model(), "messages", messages))
                    .retrieve()
                    .bodyToMono(ChatCompletionApiResponse.class)
                    .block();

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
            }

            String content = response.choices().get(0).message().content();
            long tokens = response.usage() != null ? response.usage().totalTokens() : 0L;
            return new ChatCompletionResponseDto(content, tokens);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[OpenAI] chat error: {}", e.getMessage());
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }

    @Override
    public EmbedResponseDto embed(EmbedRequestDto request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", request.model());
            body.put("input", request.input());
            if (request.dimensions() > 0) {
                body.put("dimensions", request.dimensions());
            }

            EmbedApiResponse response = webClient.post()
                    .uri("/v1/embeddings")
                    .header("Authorization", "Bearer " + request.apiKey())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(EmbedApiResponse.class)
                    .block();

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new CustomException(ErrorStatus.OPENAI_EMBEDDING_ERROR);
            }

            List<Float> embedding = response.data().get(0).embedding();
            long tokens = response.usage() != null ? response.usage().totalTokens() : 0L;
            return new EmbedResponseDto(embedding, tokens);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[OpenAI] embedding error: {}", e.getMessage());
            throw new CustomException(ErrorStatus.OPENAI_EMBEDDING_ERROR);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionApiResponse(List<Choice> choices, Usage usage) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbedApiResponse(List<EmbedData> data, Usage usage) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbedData(List<Float> embedding) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Usage(@JsonProperty("total_tokens") long totalTokens) {}
}
