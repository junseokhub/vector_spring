package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.AnswerGenerationResultDto;
import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.dto.ConversationTurn;

import java.util.List;

public interface ChatCompletionService {
    AnswerGenerationResultDto generateAnswer(
            LlmPlatform platform,
            String chatModel,
            String userText,
            String apiKey,
            List<VectorSearchRankDto> candidates,
            String systemPrompt,
            long embeddingTokens,
            List<ConversationTurn> history
    );
}
