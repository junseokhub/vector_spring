package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.*;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.content.ContentService;
import com.milvus.vector_spring.content.dto.ContentDto;
import com.milvus.vector_spring.milvus.VectorSearchService;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.util.properties.KafkaProperties;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ProjectService projectService;
    private final VectorSearchService vectorSearchService;
    private final ContentService contentService;
    private final ChatCompletionService chatCompletionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public ChatResponseDto chat(ChatRequestDto requestDto) {
        try {
            // 1. Project 조회 및 검증
            Project project = projectService.findOneProjectByKey(requestDto.getProjectKey());

            String secretKey = projectService.decryptOpenAiKey(project);
            LocalDateTime inputTime = LocalDateTime.now();

            // 2. Embedding 생성
            CreateEmbeddingResponse embedding = vectorSearchService.createEmbedding(
                    secretKey, requestDto.getText(), project.getDimensions());

            // 3. Vector 검색
            VectorSearchResponseDto searchResp = vectorSearchService.searchVector(embedding, project.getId());

            // 4. 답변 생성
            AnswerGenerationResultDto answer = chatCompletionService.generateAnswerWithDecision(
                    project.getChatModel(), requestDto.getText(), secretKey,
                    vectorSearchService.convertToRankList(searchResp),
                    searchResp, project.getPrompt(), embedding);

            // 5. 답변 여부에 따라 Content 노출
            Content finalContent = Optional.of(answer)
                    .filter(result -> !result.isPromptAnswer())
                    .map(result -> searchResp.getFirstSearchId())
                    .map(contentService::findOneContentByContentId)
                    .orElse(null);

            LocalDateTime outputTime = LocalDateTime.now();

            // 6. 결과 객체 생성
            ChatProcessResultDto result = createProcessResult(
                    requestDto.getSessionId(), inputTime, outputTime, searchResp, finalContent, answer
            );

            // 7. [Kafka로 전환]
            publishChatTask(project.getKey(), answer.getTotalToken(), requestDto, result);

            // 8. 응답 반환
            return buildResponse(requestDto, result);

        } catch (Exception e) {
            throw (e instanceof CustomException) ? (CustomException) e :
                    new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void publishChatTask(String projectKey, long totalToken, ChatRequestDto requestDto, ChatProcessResultDto result) {

        ContentDto contentDto = (result.getContent() != null) ? new ContentDto(result.getContent()) : null;

        ChatCompleteEvent event = ChatCompleteEvent.builder()
                .projectKey(projectKey)
                .totalToken(totalToken)
                .sessionId(requestDto.getSessionId())
                .input(requestDto.getText())
                .output(result.getFinalAnswer())
                .content(contentDto)
                .rankList(result.getRankList())
                .inputDateTime(result.getInputDateTime())
                .outputDateTime(result.getOutputDateTime())
                .build();

        kafkaTemplate.send(kafkaProperties.topic(), event.getSessionId(), event);
    }

    private ChatProcessResultDto createProcessResult(
            String sessionId,
            LocalDateTime inputTime,
            LocalDateTime outputTime,
            VectorSearchResponseDto searchResp,
            Content content,
            AnswerGenerationResultDto answer
    ) {

        return new ChatProcessResultDto(
                sessionId,
                answer.getFinalAnswer(),
                inputTime,
                outputTime,
                content,
                vectorSearchService.convertToRankList(searchResp),
                searchResp.getSearch()
        );
    }


    private ChatResponseDto buildResponse(
            ChatRequestDto requestDto,
            ChatProcessResultDto result
    ) {

        return ChatResponseDto.from(
                requestDto.getProjectKey(),
                result.getSessionId(),
                requestDto.getText(),
                result.getFinalAnswer(),
                result.getInputDateTime(),
                result.getOutputDateTime(),
                result.getSearchResp(),
                result.getContent()
        );
    }
}