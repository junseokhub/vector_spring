package com.milvus.vector_spring.chat.dto;

import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.content.dto.ContentDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ChatResponseDto {

    private final String sessionId;
    private final String projectKey;
    private final String input;
    private final String output;
    private final String vectorOutput;
    private final LocalDateTime inputDateTime;
    private final LocalDateTime outputDateTime;
    private final List<VectorSearchRankDto> rank;
    private final ContentDto content;

    public ChatResponseDto(String sessionId, String projectKey, String input, String output,
                           String vectorOutput, ContentDto content,
                           LocalDateTime inputDateTime, LocalDateTime outputDateTime,
                           List<VectorSearchRankDto> rank) {
        this.sessionId = sessionId;
        this.projectKey = projectKey;
        this.input = input;
        this.output = output;
        this.vectorOutput = vectorOutput;
        this.content = content;
        this.inputDateTime = inputDateTime;
        this.outputDateTime = outputDateTime;
        this.rank = rank;
    }

    public static ChatResponseDto from(
            String projectKey, String sessionId,
            String input, String output,
            LocalDateTime inputDateTime, LocalDateTime outputDateTime,
            List<VectorSearchRankDto> rankList, Content content) {

        String firstAnswer = rankList.stream()
                .findFirst()
                .map(VectorSearchRankDto::getAnswer)
                .orElse("");

        return new ChatResponseDto(
                sessionId,
                projectKey,
                input,
                output,
                firstAnswer,
                content == null ? null : new ContentDto(content),
                inputDateTime,
                outputDateTime,
                rankList
        );
    }
}
