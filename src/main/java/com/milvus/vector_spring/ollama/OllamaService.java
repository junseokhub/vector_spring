package com.milvus.vector_spring.ollama;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OllamaService {

    private final OllamaEmbeddingModel ollamaEmbeddingModel;

    public float[] embed(String text) {
        return ollamaEmbeddingModel.embed(text);
    }
}
