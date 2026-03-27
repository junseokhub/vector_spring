package com.milvus.vector_spring.libraryopenai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.libraryopenai.dto.OpenAiChatLibraryRequestDto;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OpenAiLibraryServiceImpl implements OpenAiLibraryService {

    private final Cache<String, OpenAIClient> clientCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(100)
            .build();

    private OpenAIClient connectOpenAI(String openAiKey) {
        return clientCache.get(openAiKey, key -> OpenAIOkHttpClient.builder().apiKey(key).build());
    }

    @Override
    public ChatCompletion chat(OpenAiChatLibraryRequestDto openAiChatLibraryRequestDto) {
        try {
            ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                    .addUserMessage(openAiChatLibraryRequestDto.getUserMessages())
                    .model(openAiChatLibraryRequestDto.getModel());

            if (openAiChatLibraryRequestDto.getSystemMessages() != null &&
                    !openAiChatLibraryRequestDto.getSystemMessages().isBlank()) {
                builder.addSystemMessage(openAiChatLibraryRequestDto.getSystemMessages());
            }

            ChatCompletionCreateParams params = builder.build();

            return connectOpenAI(openAiChatLibraryRequestDto.getOpenAiKey())
                    .chat()
                    .completions()
                    .create(params);
        } catch (Exception e) {
            log.error("OpenAI chat 호출 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }

    @Override
    public CreateEmbeddingResponse embedding(String openAiKey, String input, long dimension, String embedModel) {
        try {
            EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                    .model(embedModel)
                    .dimensions(dimension)
                    .input(input)
                    .build();
            return connectOpenAI(openAiKey).embeddings().create(params);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.OPENAI_EMBEDDING_ERROR);
        }
    }
}
