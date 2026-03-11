package com.milvus.vector_spring.chat.dto;

import com.milvus.vector_spring.content.dto.ContentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompleteEvent {
    private String projectKey;
    private long totalToken;

    private String sessionId;
    private String input;
    private String output;
    private ContentDto content;
    private List<VectorSearchRankDto> rankList;
    private LocalDateTime inputDateTime;
    private LocalDateTime outputDateTime;
}