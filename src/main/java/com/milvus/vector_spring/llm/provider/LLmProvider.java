package com.milvus.vector_spring.llm.provider;

import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.dto.ChatCompletionRequestDto;
import com.milvus.vector_spring.llm.dto.ChatCompletionResponseDto;
import com.milvus.vector_spring.llm.dto.EmbedRequestDto;
import com.milvus.vector_spring.llm.dto.EmbedResponseDto;

public interface LlmProvider {

    boolean supports(LlmPlatform platform);

    ChatCompletionResponseDto chat(ChatCompletionRequestDto request);

    EmbedResponseDto embed(EmbedRequestDto request);
}
