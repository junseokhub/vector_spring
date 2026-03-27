package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.ChatCompleteEvent;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.mongo.document.ChatResponseDocument;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.util.properties.KafkaProperties;
import org.springframework.dao.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatTaskConsumer {

    private final ProjectService projectService;
    private final MongoTemplate mongoTemplate;
    private final KafkaProperties kafkaProperties;

    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            exclude = {NullPointerException.class}
    )
    @KafkaListener(
            topics = "${spring.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePostChatTask(ChatCompleteEvent event) {
        try {
            ChatResponseDocument doc = ChatResponseDocument.from(event);

            try {
                mongoTemplate.save(doc);
                log.info("새로운 채팅 결과 적재 성공: {}", event.getSessionId());
            } catch (DuplicateKeyException e) {
                log.warn("이미 적재된 데이터입니다(중복 키). 토큰 합산 로직으로 넘어갑니다: {}", event.getSessionId());
                return;
            }

            projectService.plusTotalToken(event.getProjectKey(), event.getTotalToken());

        } catch (Exception e) {
            log.error("적재 프로세스 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @DltHandler
    public void handleDlt(ChatCompleteEvent event,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {

        log.error("=== DLT 인입 Report ===");
        log.error("원래 토픽: {}", topic);
        log.error("에러 내용: {}", errorMessage);
        log.error("실패 데이터: {}", event);
    }
}