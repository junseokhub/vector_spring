package com.milvus.vector_spring.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.WebClientConfig;
import com.milvus.vector_spring.util.properties.OpenAiProperties;
import com.milvus.vector_spring.openai.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {

    private final OpenAiProperties openAiProperties;

    private final WebClientConfig webClientConfig;

    private WebClient connect(String url, String openAiKey) {
        return webClientConfig.webClientBuilder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + openAiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public OpenAiChatResponseDto chat(String openAiKey, OpenAiChatRequestDto openAiChatRequestDto) throws CustomException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestBody = Map.of(
                "model", openAiChatRequestDto.getModel(),
                "messages", openAiChatRequestDto.getMessages()
        );

        try {
            String res = connect(openAiProperties.url(), openAiKey).post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(res, OpenAiChatResponseDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }

    @Override
    public OpenAiEmbedResponseDto embedding(String openAiKey, EmbedRequestDto embedRequestDto) throws CustomException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestBody = Map.of(
                "model", embedRequestDto.getEmbedModel(),
                "dimension", embedRequestDto.getDimension(),
                "input", embedRequestDto.getEmbedText()
        );
        try {
            String res = connect(openAiProperties.embedUrl(), openAiKey).post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return objectMapper.readValue(res, OpenAiEmbedResponseDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }

    @Override
    public OpenAiZodResponseDto zod(String openAiKey, OpenAiChatRequestDto openAiChatRequestDto) throws CustomException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseFormat = Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "result",
                        "schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "res", Map.of(
                                                "type", "array",
                                                "items", Map.of(
                                                        "type", "object",
                                                        "properties", Map.of(
                                                                "title", Map.of("type", "string"),
                                                                "answer", Map.of("type", "string")
                                                        ),
                                                        "required", List.of("title", "answer"),
                                                        "additionalProperties", false
                                                )
                                        )
                                ),
                                "strict", true
                        )
                )
        );
        Map<String, Object> requestBody = Map.of(
                "model", openAiChatRequestDto.getModel(),
                "messages", openAiChatRequestDto.getMessages(),
                "response_format", responseFormat
        );

        try {
            String res = connect(openAiProperties.url(), openAiKey).post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return objectMapper.readValue(res, OpenAiZodResponseDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }
}
