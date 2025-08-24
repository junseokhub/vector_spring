package com.milvus.vector_spring.chat.dto;

import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Getter;

import java.util.List;

@Getter
public class VectorSearchResponseDto {
    private SearchResp search;
    private Long firstSearchId;
    private List<String> answers;


    public VectorSearchResponseDto(SearchResp search, Long firstSearchId, List<String> answers) {
        this.search = search;
        this.firstSearchId = firstSearchId;
        this.answers = answers;
    }

}
