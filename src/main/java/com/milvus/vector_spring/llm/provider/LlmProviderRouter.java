package com.milvus.vector_spring.llm.provider;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.dto.ChatCompletionRequestDto;
import com.milvus.vector_spring.llm.dto.ChatCompletionResponseDto;
import com.milvus.vector_spring.llm.dto.EmbedRequestDto;
import com.milvus.vector_spring.llm.dto.EmbedResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlmProviderRouter {

    private final List<LlmProvider> providers;

    public LlmProviderRouter(List<LlmProvider> providers) {
        this.providers = providers;
    }

    public ChatCompletionResponseDto chat(ChatCompletionRequestDto request) {
        return resolve(request.platform()).chat(request);
    }

    public EmbedResponseDto embed(EmbedRequestDto request) {
        return resolve(request.platform()).embed(request);
    }

    private LlmProvider resolve(LlmPlatform platform) {
        return providers.stream()
                .filter(p -> p.supports(platform))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNKNOWING_MODEL));
    }
}
