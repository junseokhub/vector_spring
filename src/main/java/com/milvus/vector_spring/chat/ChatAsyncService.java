package com.milvus.vector_spring.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milvus.vector_spring.chat.dto.*;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.libraryopenai.OpenAiLibraryService;
import com.milvus.vector_spring.milvus.VectorSearchService;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.util.properties.KafkaProperties;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAsyncService {

    private static final long TIMEOUT_SECONDS = 15L;
    private static final long EMBEDDING_CACHE_TTL_HOURS = 24L;
    private static final String EMBEDDING_CACHE_PREFIX = "emb:";

    private final ProjectService projectService;
    private final VectorSearchService vectorSearchService;
    private final ChatCompletionService chatCompletionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OpenAiLibraryService openAiLibraryService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Qualifier("ioExecutor")
    private final Executor ioExecutor;

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

    // ── Step 1. 임베딩 (캐시 우선) ────────────────────────────────────────────
    private EmbeddingContext createEmbeddingStep(String secretKey,
                                                 ChatRequestDto requestDto,
                                                 Project project) {
        String normalizedText = normalize(requestDto.getText());
        String cacheKey = buildEmbeddingCacheKey(project.getId(), normalizedText);

        // 1. 캐시 조회 (String으로 읽기)
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                // String을 객체로 수동 역직렬화
                CreateEmbeddingResponse cachedEmbedding = objectMapper.readValue(
                        cached.toString(), CreateEmbeddingResponse.class);
                log.info("[Embedding Cache HIT] key={}", cacheKey);
                return new EmbeddingContext(cachedEmbedding);
            } catch (Exception e) {
                log.warn("[Embedding Cache Read Error] 캐시가 깨졌거나 형식이 다릅니다. 새로 호출합니다.", e);
            }
        }

        // 2. 캐시 미스 시 API 호출
        log.info("[Embedding Cache MISS] key={}", cacheKey);
        log.info("111111111111111: {}", project.getDimensions());
        CreateEmbeddingResponse embedding = openAiLibraryService.embedding(
                secretKey, requestDto.getText(),
                project.getDimensions(), project.getEmbedModel());

        // 3. 저장 시 String으로 변환하여 저장
        try {
            String jsonString = objectMapper.writeValueAsString(embedding);
            redisTemplate.opsForValue().set(cacheKey, jsonString, EMBEDDING_CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("[Embedding Cache Write Error] 저장 실패", e);
        }

        return new EmbeddingContext(embedding);
    }

    // ── Step 2. 벡터서치 ──────────────────────────────────────────────────────
    private SearchContext vectorSearchStep(EmbeddingContext embeddingCtx, Project project) {
        VectorSearchResponseDto searchResp = vectorSearchService.searchVector(
                embeddingCtx.embedding(), project.getId());
        return new SearchContext(embeddingCtx.embedding(), searchResp);
    }

    // ── Step 3. 답변 생성 ─────────────────────────────────────────────────────
    private AnswerContext generateAnswerStep(SearchContext searchCtx,
                                             Project project,
                                             ChatRequestDto requestDto,
                                             String secretKey) {
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

    // ── Step 4. 후처리 ────────────────────────────────────────────────────────
    private ChatResponseDto postProcessStep(AnswerContext answerCtx,
                                            ChatRequestDto requestDto,
                                            Project project,
                                            LocalDateTime inputTime) {
        LocalDateTime outputTime = LocalDateTime.now();
        log.info("=== [Step 4] 답변 생성 완료: {} ===", answerCtx.answer().getFinalAnswer());
        // Content 조회는 Kafka Consumer에서 처리하므로 메인 흐름에서 제거
        publishChatEvent(project.getKey(), answerCtx, requestDto, inputTime, outputTime);
        log.info("=== [Step 4] 최종 응답 객체 생성 완료 ===");
        return buildResponse(requestDto, answerCtx, inputTime, outputTime);
    }

    // ── Kafka 이벤트 발행 ─────────────────────────────────────────────────────
    private void publishChatEvent(String projectKey,
                                  AnswerContext answerCtx,
                                  ChatRequestDto requestDto,
                                  LocalDateTime inputTime,
                                  LocalDateTime outputTime) {

        ChatCompleteEvent event = ChatCompleteEvent.builder()
                .projectKey(projectKey)
                .totalToken(answerCtx.answer().getTotalToken())
                .sessionId(requestDto.getSessionId())
                .input(requestDto.getText())
                .output(answerCtx.answer().getFinalAnswer())
                .content(null)
                .rankList(vectorSearchService.convertToRankList(answerCtx.searchResp()))
                .inputDateTime(inputTime)
                .outputDateTime(outputTime)
                .build();

        kafkaTemplate.send(kafkaProperties.topic(), event.getSessionId(), event);
    }

    private ChatResponseDto buildResponse(ChatRequestDto requestDto,
                                          AnswerContext answerCtx,
                                          LocalDateTime inputTime,
                                          LocalDateTime outputTime) {
        return ChatResponseDto.from(
                requestDto.getProjectKey(),
                requestDto.getSessionId(),
                requestDto.getText(),
                answerCtx.answer().getFinalAnswer(),
                inputTime,
                outputTime,
                vectorSearchService.convertToRankList(answerCtx.searchResp()),
                null
        );
    }

    // ── 예외 처리 ─────────────────────────────────────────────────────────────
    private ChatResponseDto handleException(Throwable e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        log.error("=== [Async Pipeline Error] === type: {}, message: {}",
                cause.getClass().getSimpleName(), cause.getMessage(), cause);
        if (cause instanceof CustomException ce) throw ce;
        if (cause instanceof TimeoutException) throw new CustomException(ErrorStatus.CHAT_TIMEOUT_ERROR);
        throw new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, cause);
    }

    // ── 텍스트 정규화 (캐시 히트율 향상) ─────────────────────────────────────
    private String normalize(String text) {
        return text.trim()
                .toLowerCase()
                .replaceAll("[?!。？！~]", "")
                .replaceAll("\\s+", " ");
    }

    // ── 임베딩 캐시 키 생성 ───────────────────────────────────────────────────
    private String buildEmbeddingCacheKey(Long projectId, String normalizedText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedText.getBytes(StandardCharsets.UTF_8));
            return EMBEDDING_CACHE_PREFIX + projectId + ":" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return EMBEDDING_CACHE_PREFIX + projectId + ":" + normalizedText;
        }
    }

    // ── Context Records ───────────────────────────────────────────────────────
    private record EmbeddingContext(CreateEmbeddingResponse embedding) {}
    private record SearchContext(CreateEmbeddingResponse embedding, VectorSearchResponseDto searchResp) {}
    private record AnswerContext(VectorSearchResponseDto searchResp, AnswerGenerationResultDto answer) {}
}
