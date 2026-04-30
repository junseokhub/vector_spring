package com.milvus.vector_spring.llm;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LlmModelRegistry {

    private static final List<LlmModelInfo> MODELS = List.of(

            // ── OpenAI Chat ────────────────────────────────────────────────────────
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.CHAT, "gpt-4o",       null, false),
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.CHAT, "gpt-4o-mini",  null, false),
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.CHAT, "gpt-4-turbo",  null, false),
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.CHAT, "gpt-3.5-turbo",null, false),
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.CHAT, "o3-mini",      null, false),

            // ── OpenAI Embed ───────────────────────────────────────────────────────
            // text-embedding-3-small/large support custom dimensions (1..max) via API
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.EMBED, "text-embedding-3-small", 1536L, true),
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.EMBED, "text-embedding-3-large", 3072L, true),
            new LlmModelInfo(LlmPlatform.OPENAI, ModelType.EMBED, "text-embedding-ada-002", 1536L, false),

            // ── Ollama Chat ────────────────────────────────────────────────────────
            new LlmModelInfo(LlmPlatform.OLLAMA, ModelType.CHAT, "llama3.2:3b",    null, false),
            new LlmModelInfo(LlmPlatform.OLLAMA, ModelType.CHAT, "gemma3:4b",      null, false),


            // ── Ollama Embed ───────────────────────────────────────────────────────
            new LlmModelInfo(LlmPlatform.OLLAMA, ModelType.EMBED, "bge-m3:latest", 1024L, false)
    );

    public List<LlmModelInfo> findAll() {
        return MODELS;
    }

    public List<LlmModelInfo> findByPlatform(LlmPlatform platform) {
        return MODELS.stream()
                .filter(m -> m.platform() == platform)
                .toList();
    }

    public List<LlmModelInfo> findByPlatformAndType(LlmPlatform platform, ModelType type) {
        return MODELS.stream()
                .filter(m -> m.platform() == platform && m.type() == type)
                .toList();
    }

    public List<LlmModelInfo> findByType(ModelType type) {
        return MODELS.stream()
                .filter(m -> m.type() == type)
                .toList();
    }

    public Optional<LlmModelInfo> findByName(String name) {
        return MODELS.stream()
                .filter(m -> m.name().equals(name))
                .findFirst();
    }


    public void validateEmbedModel(LlmPlatform platform, String modelName, long requestedDimensions) {
        LlmModelInfo model = findByName(modelName)
                .orElseThrow(() -> new CustomException(ErrorStatus.INVALID_MODEL));

        if (model.type() != ModelType.EMBED) {
            throw new CustomException(ErrorStatus.INVALID_MODEL);
        }
        if (model.platform() != platform) {
            throw new CustomException(ErrorStatus.INVALID_MODEL);
        }

        long maxDims = model.dimensions();
        if (model.flexDimensions()) {
            if (requestedDimensions < 1 || requestedDimensions > maxDims) {
                throw new CustomException(ErrorStatus.DIMENSION_MISMATCH);
            }
        } else {
            if (requestedDimensions != maxDims) {
                throw new CustomException(ErrorStatus.DIMENSION_MISMATCH);
            }
        }
    }


    public void validateChatModel(LlmPlatform platform, String modelName) {
        LlmModelInfo model = findByName(modelName)
                .orElseThrow(() -> new CustomException(ErrorStatus.INVALID_MODEL));

        if (model.type() != ModelType.CHAT) {
            throw new CustomException(ErrorStatus.INVALID_MODEL);
        }
        if (model.platform() != platform) {
            throw new CustomException(ErrorStatus.INVALID_MODEL);
        }
    }
}
