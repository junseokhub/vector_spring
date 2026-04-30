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
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.milvus.vector_spring.llm.dto.ConversationTurn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OllamaProvider implements LlmProvider {

    private final WebClient webClient;

    public OllamaProvider(LlmProperties llmProperties) {
        this.webClient = WebClient.builder()
                .baseUrl(llmProperties.ollama().baseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public boolean supports(LlmPlatform platform) {
        return platform == LlmPlatform.OLLAMA;
    }

    @Override
    public ChatCompletionResponseDto chat(ChatCompletionRequestDto request) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (request.systemMessage() != null && !request.systemMessage().isBlank()) {
                messages.add(Map.of("role", "system", "content", request.systemMessage()));
            }
            for (ConversationTurn turn : request.history()) {
                messages.add(Map.of("role", turn.role(), "content", turn.content()));
            }
            messages.add(Map.of("role", "user", "content", request.userMessage()));

            OllamaChatResponse response = webClient.post()
                    .uri("/api/chat")
                    .bodyValue(Map.of(
                            "model", request.model(),
                            "messages", messages,
                            "stream", false
                    ))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> res.bodyToMono(String.class)
                            .doOnNext(body -> log.error("[Ollama] chat error body: {}", body))
                            .map(body -> new RuntimeException("Ollama error: " + body)))
                    .bodyToMono(OllamaChatResponse.class)
                    .block();

            if (response == null || response.message() == null) {
                throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
            }

            long totalTokens = 0L;
            if (response.promptEvalCount() != null) totalTokens += response.promptEvalCount();
            if (response.evalCount() != null) totalTokens += response.evalCount();

            return new ChatCompletionResponseDto(response.message().content(), totalTokens);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Ollama] chat error: {}", e.getMessage());
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }

    @Override
    public EmbedResponseDto embed(EmbedRequestDto request) {
        try {
            OllamaEmbedResponse response = webClient.post()
                    .uri("/api/embed")
                    .bodyValue(Map.of(
                            "model", request.model(),
                            "input", request.input()
                    ))
                    .retrieve()
                    .bodyToMono(OllamaEmbedResponse.class)
                    .block();

            if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
                throw new CustomException(ErrorStatus.OPENAI_EMBEDDING_ERROR);
            }

            long totalTokens = response.promptEvalCount() != null ? response.promptEvalCount() : 0L;
            return new EmbedResponseDto(response.embeddings().get(0), totalTokens);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Ollama] embedding error: {}", e.getMessage());
            throw new CustomException(ErrorStatus.OPENAI_EMBEDDING_ERROR);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaChatResponse(
            OllamaMessage message,
            @JsonProperty("prompt_eval_count") Long promptEvalCount,
            @JsonProperty("eval_count") Long evalCount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaMessage(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaEmbedResponse(
            List<List<Float>> embeddings,
            @JsonProperty("prompt_eval_count") Long promptEvalCount
    ) {}
}
