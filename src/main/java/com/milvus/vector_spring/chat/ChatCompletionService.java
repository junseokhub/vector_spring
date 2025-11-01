package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.AnswerGenerationResultDto;
import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;
import com.openai.models.embeddings.CreateEmbeddingResponse;

import java.util.List;

public interface ChatCompletionService {
//    ChatCompletion generateAnswer(String chatModel, String userText, String openAiKey, List<VectorSearchRankDto> rankList, VectorSearchResponseDto searchResponse, String prompt);
    AnswerGenerationResultDto generateAnswerWithDecision(
            String chatModel,
            String userText,
            String openAiKey,
            List<VectorSearchRankDto> rankList,
            VectorSearchResponseDto searchResponse,
            String prompt,
            CreateEmbeddingResponse embeddingResponse
    );
}
