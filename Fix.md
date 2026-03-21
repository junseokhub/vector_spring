# ChatService 비동기 리팩토링

## 배경

RAG 기반 채팅 API에서 OpenAI Embedding, Milvus Vector 검색, OpenAI Chat Completion 세 가지 외부 API를 순차 호출하는 구조였음.
동기 처리 시 서블릿 스레드가 외부 API 응답 대기 시간 내내 점유되어 처리량(Throughput) 저하 문제 존재.

---

## 1단계 — 동기 방식 (기존)

```java
CreateEmbeddingResponse embedding = vectorSearchService.createEmbedding(...);
VectorSearchResponseDto searchResp = vectorSearchService.searchVector(embedding, ...);
AnswerGenerationResultDto answer = chatCompletionService.generateAnswerWithDecision(...);
```

**문제점**
- 외부 API 호출 3개가 모두 서블릿 스레드를 블로킹
- 동시 요청 증가 시 스레드 풀 고갈 위험

---

## 2단계 — 초기 비동기 시도 (문제 있는 버전)

```java
CompletableFuture.supplyAsync(() -> projectService.findOneProjectByKey(...))
    .thenCompose(project ->
        vectorSearchService.createEmbedding(...)
            .thenCompose(embedding ->
                vectorSearchService.searchVector(...)
                    .thenCompose(searchResp ->
                        chatCompletionService.generateAnswerWithDecision(...)
                    )
            )
    )
```

**문제점**
- 4단 `thenCompose` 중첩 → 콜백 헬, 가독성 저하
- Executor 미지정 → ForkJoinPool에서 블로킹 I/O 실행, 스레드 고갈 위험 동일
- `e.getCause()` null 체크 누락 → NPE 가능성
- `orTimeout()` 미적용 → 외부 API 무응답 시 무한 대기

---

## 3단계 — 최종 리팩토링

**핵심 변경 사항**

#### Executor 명시 분리
```java
private final Executor ioExecutor = Executors.newFixedThreadPool(20);
// Java 21: Executors.newVirtualThreadPerTaskExecutor() 권장
```
블로킹 I/O 전용 스레드 풀을 분리해 ForkJoinPool 고갈 방지.

#### Context Record로 중첩 제거
```java
private record EmbeddingContext(CreateEmbeddingResponse embedding) {}
private record SearchContext(CreateEmbeddingResponse embedding, VectorSearchResponseDto searchResp) {}
private record AnswerContext(VectorSearchResponseDto searchResp, AnswerGenerationResultDto answer) {}
```
단계 간 데이터를 타입 안전하게 전달, `Map.entry()` 제거.

#### Flat 체인으로 가독성 개선
```java
CompletableFuture
    .supplyAsync(() -> createEmbeddingStep(...), ioExecutor)
    .thenApplyAsync(embeddingCtx -> vectorSearchStep(...), ioExecutor)
    .thenApplyAsync(searchCtx -> generateAnswerStep(...), ioExecutor)
    .thenApplyAsync(answerCtx -> postProcessStep(...), ioExecutor)
    .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .exceptionally(this::handleException);
```

#### 예외 처리 안전화
```java
private ChatResponseDto handleException(Throwable e) {
    Throwable cause = e.getCause() != null ? e.getCause() : e;  // NPE 방지
    if (cause instanceof CustomException ce) throw ce;
    if (cause instanceof TimeoutException) throw new CustomException(ErrorStatus.CHAT_TIMEOUT_ERROR);
    throw new CustomException(ErrorStatus.INTERNAL_SERVER_ERROR, cause);
}
```

---

## 개선 요약

| 항목 | 기존 | 최종 |
|------|------|------|
| 스레드 블로킹 | 서블릿 스레드 점유 | ioExecutor 분리로 해방 |
| 코드 구조 | 4단 중첩 콜백 | Flat 체인 + Step 메서드 분리 |
| 타임아웃 | 없음 | `orTimeout(15s)` 적용 |
| 예외 처리 | NPE 가능성 | cause null 체크 + Timeout 분기 |
| 데이터 전달 | `Map.entry()` | Context Record |