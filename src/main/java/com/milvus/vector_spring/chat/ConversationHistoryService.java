package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.config.mongo.document.ChatResponseDocument;
import com.milvus.vector_spring.llm.dto.ConversationTurn;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ConversationHistoryService {

    private static final int MAX_TURNS = 5;

    private final MongoTemplate mongoTemplate;

    public List<ConversationTurn> getRecentHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return List.of();
        }

        Query query = Query.query(Criteria.where("sessionId").is(sessionId))
                .with(Sort.by(Sort.Direction.DESC, "inputDateTime"))
                .limit(MAX_TURNS);

        List<ChatResponseDocument> docs = mongoTemplate.find(query, ChatResponseDocument.class);

        return docs.stream()
                .sorted(Comparator.comparing(ChatResponseDocument::getInputDateTime))
                .flatMap(doc -> Stream.of(
                        ConversationTurn.user(doc.getInput()),
                        ConversationTurn.assistant(doc.getOutput())
                ))
                .toList();
    }
}
