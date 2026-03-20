package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.*;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.content.ContentService;
import com.milvus.vector_spring.content.dto.ContentDto;
import com.milvus.vector_spring.libraryopenai.OpenAiLibraryService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAsyncService {

    private static final long TIMEOUT_SECONDS = 15L;

    private final Executor ioExecutor = Executors.newFixedThreadPool(20);

    private final ProjectService projectService;
    private final VectorSearchService vectorSearchService;
    private final ContentService contentService;
    private final ChatCompletionService chatCompletionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OpenAiLibraryService openAiLibraryService;

    public CompletableFuture<ChatResponseDto> chatAsync(ChatRequestDto requestDto) {
        Project project = projectService.findOneProjectByKey(requestDto.getProjectKey());
        String secretKey = projectService.decryptOpenAiKey(project);
        LocalDateTime inputTime = LocalDateTime.now();

        return CompletableFuture
                .supplyAsync(
                        () -> createEmbeddingStep(secretKey, requestDto, project),
                        ioExecutor
                )
                .thenApplyAsync(
                        embeddingCtx -> vectorSearchStep(embeddingCtx, project),
                        ioExecutor
                )
                .thenApplyAsync(
                        searchCtx -> generateAnswerStep(searchCtx, project, requestDto, secretKey),
                        ioExecutor
                )
                .thenApplyAsync(
                        answerCtx -> postProcessStep(answerCtx, requestDto, project, inputTime),
                        ioExecutor
                )
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(this::handleException);
    }


    private EmbeddingContext createEmbeddingStep(String secretKey, ChatRequestDto requestDto, Project project) {
        CreateEmbeddingResponse embedding = openAiLibraryService.embedding(
                secretKey, requestDto.getText(), project.getDimensions());
        return new EmbeddingContext(embedding);
    }

    private SearchContext vectorSearchStep(EmbeddingContext embeddingCtx, Project project) {
        VectorSearchResponseDto searchResp = vectorSearchService.searchVector(
                embeddingCtx.embedding(), project.getId());
        return new SearchContext(embeddingCtx.embedding(), searchResp);
    }

    private AnswerContext generateAnswerStep(SearchContext searchCtx, Project project,
                                             ChatRequestDto requestDto, String secretKey) {
        AnswerGenerationResultDto answer = chatCompletionService.generateAnswerWithDecision(
                project.getChatModel(),
                requestDto.getText(),
                secretKey,
                vectorSearchService.convertToRankList(searchCtx.searchResp()),
                searchCtx.searchResp(),
                project.getPrompt(),
                searchCtx.embedding()
        );
        return new AnswerContext(searchCtx.searchResp(), answer);
    }

    private ChatResponseDto postProcessStep(AnswerContext answerCtx, ChatRequestDto requestDto,
                                            Project project, LocalDateTime inputTime) {
        Content finalContent = resolveContent(answerCtx);
        LocalDateTime outputTime = LocalDateTime.now();

        ChatProcessResultDto result = buildProcessResult(
                requestDto.getSessionId(), inputTime, outputTime, answerCtx, finalContent);

        publishChatEvent(project.getKey(), answerCtx.answer().getTotalToken(), requestDto, result);

        return buildResponse(requestDto, result);
    }


    private Content resolveContent(AnswerContext answerCtx) {
        return Optional.of(answerCtx.answer())
                .filter(answer -> !answer.isPromptAnswer())
                .map(answer -> answerCtx.searchResp().getFirstSearchId())
                .map(contentService::findOneContentByContentId)
                .orElse(null);
    }

    private ChatProcessResultDto buildProcessResult(String sessionId, LocalDateTime inputTime,
                                                    LocalDateTime outputTime, AnswerContext answerCtx,
                                                    Content content) {
        return new ChatProcessResultDto(
                sessionId,
                answerCtx.answer().getFinalAnswer(),
                inputTime,
                outputTime,
                content,
                vectorSearchService.convertToRankList(answerCtx.searchResp()),
                answerCtx.searchResp().getSearch()
        );
    }

    private void publishChatEvent(String projectKey, long totalToken,
                                  ChatRequestDto requestDto, ChatProcessResultDto result) {
        ContentDto contentDto = Optional.ofNullable(result.getContent())
                .map(ContentDto::new)
                .orElse(null);

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

    private ChatResponseDto handleException(Throwable e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause instanceof CustomException ce) throw ce;
        if (cause instanceof TimeoutException) throw new CustomException(ErrorStatus.CHAT_TIMEOUT_ERROR);
        throw new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, cause);
    }

    private record EmbeddingContext(CreateEmbeddingResponse embedding) {}

    private record SearchContext(CreateEmbeddingResponse embedding, VectorSearchResponseDto searchResp) {}

    private record AnswerContext(VectorSearchResponseDto searchResp, AnswerGenerationResultDto answer) {}
}