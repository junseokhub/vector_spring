package com.milvus.vector_spring.springai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SpringAiConnector {

//    private final Map<String, OpenAiApi> apiCache = new ConcurrentHashMap<>();

    private final Cache<String, OpenAiApi> apiCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(100) // 최대 100개까지만 유지
            .build();

    public OpenAiApi getOrCreateApi(String apiKey) {
        return apiCache.get(apiKey, OpenAiApi::new);
    }

    public OpenAiChatModel getChatModel(String apiKey, String modelName) {
        return new OpenAiChatModel(getOrCreateApi(apiKey), OpenAiChatOptions.builder()
                .withModel(modelName)
                .build());
    }

    public OpenAiEmbeddingModel getEmbeddingModel(String apiKey, String modelName, Integer dimension) {
        return new OpenAiEmbeddingModel(
                getOrCreateApi(apiKey),
                MetadataMode.ALL,
                OpenAiEmbeddingOptions.builder()
                        .withModel(modelName)
                        .withDimensions(dimension)
                        .build()
        );
    }
}