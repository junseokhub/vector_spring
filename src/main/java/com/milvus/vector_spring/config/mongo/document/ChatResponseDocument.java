package com.milvus.vector_spring.config.mongo.document;

import com.milvus.vector_spring.chat.dto.ChatCompleteEvent;
import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.content.dto.ContentDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Document(collection = "chat_response")
public class ChatResponseDocument {

    @Id
    private String id;
    private String sessionId;
    private String projectKey;
    private String input;
    private String output;
    private ContentDto content;
    private LocalDateTime inputDateTime;
    private LocalDateTime outputDateTime;
    private List<VectorSearchRankDto> rank;

    public static ChatResponseDocument from(ChatCompleteEvent event) {
        return ChatResponseDocument.builder()
                .sessionId(event.sessionId())
                .projectKey(event.projectKey())
                .input(event.input())
                .output(event.output())
                .content(event.content())
                .rank(event.rankList())
                .inputDateTime(event.inputDateTime())
                .outputDateTime(event.outputDateTime())
                .build();
    }
}
