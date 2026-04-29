package com.milvus.vector_spring.chat.dto;

import com.milvus.vector_spring.content.dto.ContentDto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponseDto(
        String projectKey,
        String sessionId,
        String input,
        String output,
        LocalDateTime inputDateTime,
        LocalDateTime outputDateTime,
        List<VectorSearchRankDto> rankList,
        ContentDto content
) {}
