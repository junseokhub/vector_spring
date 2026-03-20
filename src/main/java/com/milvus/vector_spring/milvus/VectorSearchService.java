package com.milvus.vector_spring.milvus;

import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;
import com.openai.models.embeddings.CreateEmbeddingResponse;

import java.util.List;

public interface VectorSearchService {
    VectorSearchResponseDto searchVector(CreateEmbeddingResponse embedding, Long projectId);
    List<VectorSearchRankDto> convertToRankList(VectorSearchResponseDto searchResponse);
}