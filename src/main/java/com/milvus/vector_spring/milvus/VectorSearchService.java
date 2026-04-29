package com.milvus.vector_spring.milvus;

import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;

import java.util.List;

public interface VectorSearchService {
    VectorSearchResponseDto search(List<Float> embedding, Long projectId);
}
