package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.*;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ProjectService projectService;
    private final ContentService contentService;
    private final VectorSearchService vectorSearchService;
    private final ChatCompletionService chatCompletionService;
    private final MongoTemplate mongoTemplate;

    public Mono<ChatResponseDto> flatMap(ChatRequestDto requestDto, String sessionId) {
        LocalDateTime inputTime = LocalDateTime.now();
        log.info("[Chat 시작] ProjectKey: {}", requestDto.getProjectKey());

        return findProject(requestDto)
                .flatMap(project -> {
                    String secretKey = projectService.decryptOpenAiKey(project);

                    return createEmbedding(project, secretKey, requestDto)
                            .flatMap(embedding ->
                                    searchVector(project, embedding)
                                            .flatMap(searchResp ->
                                                    fetchContent(searchResp)
                                                            .flatMap(contentOpt ->
                                                                    generateAnswer(project, requestDto, secretKey, sessionId,
                                                                            inputTime, searchResp, embedding, contentOpt)
                                                            )
                                            )
                            );
                })
                .onErrorMap(e -> e instanceof CustomException ? e : new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, e));
    }

    public Mono<ChatResponseDto> chat(ChatRequestDto requestDto, String sessionId) {
        return findProject(requestDto)
                .flatMap(project -> {
                    LocalDateTime inputTime = LocalDateTime.now();
                    String secretKey = projectService.decryptOpenAiKey(project);

                    return createEmbedding(project, secretKey, requestDto)
                            .flatMap(embedding ->
                                    searchVector(project, embedding)
                                            .flatMap(searchResp ->
                                                    Mono.zip(
                                                            Mono.just(searchResp),
                                                            Mono.just(embedding),
                                                            fetchContent(searchResp)
                                                    )
                                            )
                                            .flatMap(tuple -> {
                                                return generateAnswer(
                                                        project, requestDto, secretKey, sessionId,
                                                        inputTime, tuple.getT1(), tuple.getT2(), tuple.getT3()
                                                );
                                            })
                            );
                })
                .onErrorMap(e -> (e instanceof CustomException) ? e : new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, e));
    }

    /**
     * Project 조회 (Blocking -> Mono)
     */
    private Mono<Project> findProject(ChatRequestDto requestDto) {
        return Mono.fromCallable(() -> projectService.findOneProjectByKey(requestDto.getProjectKey()))
                .subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new CustomException(ErrorStatus.NOT_FOUND_PROJECT)))
                .filter(p -> p.getOpenAiKey() != null && !p.getOpenAiKey().isEmpty()
                        && p.getChatModel() != null && !p.getChatModel().isEmpty())
                .switchIfEmpty(Mono.error(new CustomException(ErrorStatus.REQUIRE_OPEN_AI_INFO)));
    }

    /**
     * Embedding 생성 (Blocking -> Mono)
     */
    private Mono<CreateEmbeddingResponse> createEmbedding(Project project, String secretKey, ChatRequestDto requestDto) {
        return Mono.fromCallable(() -> vectorSearchService.createEmbedding(
                        secretKey, requestDto.getText(), project.getDimensions()))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Embedding 생성 실패:", e));
    }

    /**
     * Vector 검색 (Blocking -> Mono)
     */
    private Mono<VectorSearchResponseDto> searchVector(Project project, CreateEmbeddingResponse embedding) {
        return Mono.fromCallable(() -> vectorSearchService.searchVector(embedding, project.getId()))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Vector Search 실패:", e));
    }

    /**
     * Content 조회 (Blocking -> Mono)
     */
    private Mono<Optional<Content>> fetchContent(VectorSearchResponseDto searchResp) {
        Long contentId = searchResp.getFirstSearchId();

        if (contentId == null) {
            return Mono.just(Optional.empty());
        }

        return Mono.fromCallable(() ->
                        contentService.findOneContentByContentId(contentId)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Content 조회 실패:", e));
    }

    /**
     * GPT 답변 생성 (Blocking -> Mono)
     */
    private Mono<ChatResponseDto> generateAnswer(
            Project project, ChatRequestDto requestDto, String secretKey, String sessionId,
            LocalDateTime inputTime, VectorSearchResponseDto searchResp,
            CreateEmbeddingResponse embedding, Optional<Content> contentOpt
    ) {
        return Mono.fromCallable(() -> chatCompletionService.generateAnswerWithDecision(
                        project.getChatModel(), requestDto.getText(), secretKey,
                        vectorSearchService.convertToRankList(searchResp),
                        searchResp, project.getPrompt(), embedding))
                .subscribeOn(Schedulers.boundedElastic())
                .map(answer -> {
                    LocalDateTime actualOutputTime = LocalDateTime.now();

                    ChatProcessResultDto result = createProcessResult(
                            sessionId, inputTime, actualOutputTime, searchResp, contentOpt, answer
                    );

                    processPostChatTasks(project.getKey(), answer.getTotalToken(), requestDto, result)
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe();

                    return buildResponse(requestDto, result);
                });
    }

    private Mono<Void> processPostChatTasks(String projectKey, long totalToken, ChatRequestDto requestDto, ChatProcessResultDto result) {
        return Mono.fromRunnable(() -> {
            try {
                projectService.plusTotalToken(projectKey, totalToken);
                mongoTemplate.save(createDocument(requestDto, result));
                log.info("[사후 작업 완료]");
            } catch (Exception e) {
                log.error("[사후 작업 실패]", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private ChatProcessResultDto createProcessResult(
            String sessionId,
            LocalDateTime inputTime,
            LocalDateTime outputTime,
            VectorSearchResponseDto searchResp,
            Optional<Content> content,
            AnswerGenerationResultDto answer
    ) {

        return new ChatProcessResultDto(
                sessionId,
                answer.getFinalAnswer(),
                inputTime,
                outputTime,
                content.orElse(null),
                vectorSearchService.convertToRankList(searchResp),
                searchResp.getSearch()
        );
    }

    private ChatResponseDocument createDocument(
            ChatRequestDto requestDto,
            ChatProcessResultDto result
    ) {

        ChatResponseDocument.SimpleContentDto simpleContent =
                Optional.ofNullable(result.getContent())
                        .map(c -> new ChatResponseDocument.SimpleContentDto(
                                c.getId(),
                                c.getKey(),
                                c.getTitle(),
                                c.getAnswer()
                        ))
                        .orElseGet(() ->
                                new ChatResponseDocument.SimpleContentDto(null, null, null, null)
                        );

        return new ChatResponseDocument(
                result.getSessionId(),
                requestDto.getText(),
                result.getFinalAnswer(),
                result.getInputDateTime(),
                result.getOutputDateTime(),
                simpleContent,
                result.getRankList()
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