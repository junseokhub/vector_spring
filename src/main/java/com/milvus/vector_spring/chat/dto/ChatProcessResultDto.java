package com.milvus.vector_spring.chat.dto;

import com.milvus.vector_spring.content.Content;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ChatProcessResultDto {
    private final String sessionId;
    private final String finalAnswer;


    private final LocalDateTime inputDateTime;
    private final LocalDateTime outputDateTime;
    private final Content content;
    private final List<VectorSearchRankDto> rankList;
    private final SearchResp searchResp;

    public ChatProcessResultDto(String sessionId, String finalAnswer, LocalDateTime inputDateTime, LocalDateTime outputDateTime, Content content, List<VectorSearchRankDto> rankList, SearchResp searchResp) {
        this.sessionId = sessionId;
        this.finalAnswer = finalAnswer;
        this.inputDateTime = inputDateTime;
        this.outputDateTime = outputDateTime;
        this.content = content;
        this.rankList = rankList;
        this.searchResp = searchResp;
    }
}