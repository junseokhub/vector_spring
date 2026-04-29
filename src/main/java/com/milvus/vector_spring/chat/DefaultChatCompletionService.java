package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.AnswerGenerationResultDto;
import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.dto.ChatCompletionRequestDto;
import com.milvus.vector_spring.llm.dto.ChatCompletionResponseDto;
import com.milvus.vector_spring.llm.provider.LlmProviderRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultChatCompletionService implements ChatCompletionService {

    private static final double CONFIDENCE_THRESHOLD = 0.5;

    private final LlmProviderRouter llmProviderRouter;

    @Override
    public AnswerGenerationResultDto generateAnswer(
            LlmPlatform platform,
            String chatModel, String userText, String apiKey,
            List<VectorSearchRankDto> candidates, String systemPrompt,
            long embeddingTokens
    ) {
        // No chat model configured — return best vector search result without calling LLM
        if (chatModel == null || chatModel.isBlank()) {
            if (!candidates.isEmpty()) {
                return new AnswerGenerationResultDto(
                        candidates.get(0).answer(), embeddingTokens, LocalDateTime.now(), false
                );
            }
            return new AnswerGenerationResultDto(
                    "No matching answer found.", embeddingTokens, LocalDateTime.now(), false
            );
        }

        boolean isHighConfidence = !candidates.isEmpty() && candidates.get(0).score() > CONFIDENCE_THRESHOLD;

        if (isHighConfidence) {
            return new AnswerGenerationResultDto(
                    candidates.get(0).answer(),
                    embeddingTokens,
                    LocalDateTime.now(),
                    false
            );
        }

        return callLlm(platform, chatModel, userText, apiKey, candidates, systemPrompt, embeddingTokens);
    }

    private AnswerGenerationResultDto callLlm(
            LlmPlatform platform,
            String chatModel, String userText, String apiKey,
            List<VectorSearchRankDto> candidates, String systemPrompt,
            long embeddingTokens
    ) {
        try {
            String resolvedPrompt = (systemPrompt == null || systemPrompt.isBlank())
                    ? buildDefaultPrompt(userText, candidates.stream().map(VectorSearchRankDto::answer).toList())
                    : systemPrompt;

            ChatCompletionResponseDto response = llmProviderRouter.chat(
                    ChatCompletionRequestDto.from(platform, apiKey, chatModel, userText, resolvedPrompt)
            );

            return new AnswerGenerationResultDto(
                    response.content(),
                    embeddingTokens + response.totalTokens(),
                    LocalDateTime.now(),
                    true
            );
        } catch (Exception e) {
            log.error("[ChatCompletion] LLM call failed: {}", e.getMessage(), e);
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }

    private String buildDefaultPrompt(String userText, List<String> knowledgeBase) {
        return """
                You are a helpful assistant. Analyze the user's question and answer based on the provided knowledge base.
                If the knowledge base does not contain relevant information, inform the user and suggest trying a different question.
                Always respond in the same language as the user's input.

                User question: %s
                Knowledge base: %s
                """.formatted(userText, knowledgeBase);
    }
}
