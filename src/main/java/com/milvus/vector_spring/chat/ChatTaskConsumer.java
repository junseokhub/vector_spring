package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.ChatCompleteEvent;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.mongo.document.ChatResponseDocument;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.util.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatTaskConsumer {

    private final ProjectService projectService;
    private final MongoTemplate mongoTemplate;
    private final KafkaProperties kafkaProperties;

    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePostChatTask(ChatCompleteEvent event) {
        try {
            projectService.plusTotalToken(event.getProjectKey(), event.getTotalToken());

            ChatResponseDocument doc = ChatResponseDocument.from(event);
            mongoTemplate.save(doc);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}