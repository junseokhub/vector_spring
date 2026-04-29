package com.milvus.vector_spring.chat.dto;

import java.util.List;

public record VectorSearchResponseDto(
        Long firstSearchId,
        List<VectorSearchRankDto> results
) {}
