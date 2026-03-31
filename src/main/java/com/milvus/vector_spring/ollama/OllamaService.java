package com.milvus.vector_spring.ollama;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OllamaService {

    private final OllamaEmbeddingModel ollamaEmbeddingModel;

    public OllamaService() {
        var ollamaApi = new OllamaApi("http://192.168.0.208:11434");
        var options = OllamaOptions.create().withModel("bge-m3");

        this.ollamaEmbeddingModel = new OllamaEmbeddingModel(ollamaApi, options);
    }

    public float[] embed(String text) {
        return ollamaEmbeddingModel.embed(text);
    }
}
