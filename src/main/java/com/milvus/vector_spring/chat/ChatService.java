package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.*;
import com.milvus.vector_spring.common.apipayload.BaseCode;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.mongo.document.ChatResponseDocument;
import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.content.ContentService;
import com.milvus.vector_spring.milvus.VectorSearchService;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    @Value("${open.ai.key}")
    private String openAiKey;

    private final ProjectService projectService;
    private final ContentService contentService;
    private final VectorSearchService vectorSearchService;
    private final ChatCompletionService chatCompletionService;
    private final MongoTemplate mongoTemplate;

    public ChatResponseDto chat(ChatRequestDto chatRequestDto, String sessionId) {
        LocalDateTime inputDateTime = LocalDateTime.now();

        try {
            Project project = projectService.findOneProjectByKey(chatRequestDto.getProjectKey());
            if (project.getOpenAiKey().isEmpty() && project.getChatModel().isEmpty()) {
                throw new CustomException(ErrorStatus.REQUIRE_OPEN_AI_INFO);
            }

            String secretKey = projectService.decryptOpenAiKey(project);
            CreateEmbeddingResponse embeddingResponse = vectorSearchService.createEmbedding(secretKey, chatRequestDto.getText(), project.getDimensions());
            VectorSearchResponseDto searchResponse = vectorSearchService.searchVector(embeddingResponse, project.getId());
            List<VectorSearchRankDto> rankList = vectorSearchService.convertToRankList(searchResponse);

            AnswerGenerationResultDto answerResult = chatCompletionService.generateAnswerWithDecision(
                    project.getChatModel(),
                    chatRequestDto.getText(),
                    secretKey,
                    rankList,
                    searchResponse,
                    project.getPrompt(),
                    embeddingResponse
            );

            Content content = Optional.ofNullable(searchResponse.getFirstSearchId())
                    .flatMap(contentService::findOneContentByContentId)
                    .orElse(null);

            LocalDateTime outputDateTime = LocalDateTime.now();
            projectService.plusTotalToken(project.getKey(), answerResult.getTotalToken());

            ChatProcessResultDto resultDto = new ChatProcessResultDto(
                    sessionId,
                    answerResult.getFinalAnswer(),
                    inputDateTime,
                    outputDateTime,
                    content,
                    rankList,
                    searchResponse.getSearch()
            );

            ChatResponseDto chatResponseDto = buildResponse(chatRequestDto, resultDto);
            saveResponse(chatRequestDto, resultDto);

            return chatResponseDto;
        } catch (Exception e) {
            throw new CustomException((BaseCode) e);
        }
    }

    private ChatResponseDto buildResponse(ChatRequestDto requestDto, ChatProcessResultDto result) {
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

    private void saveResponse(ChatRequestDto requestDto, ChatProcessResultDto result) {
        ChatResponseDocument.SimpleContentDto simpleContent = Optional.ofNullable(result.getContent())
                .map(c -> new ChatResponseDocument.SimpleContentDto(
                        c.getId(), c.getKey(), c.getTitle(), c.getAnswer()))
                .orElse(new ChatResponseDocument.SimpleContentDto(null, null, null, null));

        ChatResponseDocument doc = new ChatResponseDocument(
                result.getSessionId(),
                requestDto.getText(),
                result.getFinalAnswer(),
                result.getInputDateTime(),
                result.getOutputDateTime(),
                simpleContent,
                result.getRankList()
        );

        mongoTemplate.save(doc);
    }
}
