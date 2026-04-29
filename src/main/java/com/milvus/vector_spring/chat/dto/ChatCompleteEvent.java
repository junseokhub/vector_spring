package com.milvus.vector_spring.chat.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.milvus.vector_spring.content.dto.ContentDto;

import java.time.LocalDateTime;
import java.util.List;

@JsonDeserialize
public record ChatCompleteEvent(
        String projectKey,
        long totalToken,
        String sessionId,
        String input,
        String output,
        ContentDto content,
        List<VectorSearchRankDto> rankList,
        LocalDateTime inputDateTime,
        LocalDateTime outputDateTime
) {}