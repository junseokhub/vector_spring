package com.milvus.vector_spring.ollama;

import com.milvus.vector_spring.util.properties.OllamaProperties;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel(OllamaProperties ollamaProperties) {
        var api = new OllamaApi(ollamaProperties.baseUrl());
        var options = OllamaOptions.create()
                .withModel(ollamaProperties.embedding().options().model());

        return new OllamaEmbeddingModel(api, options);
    }
}