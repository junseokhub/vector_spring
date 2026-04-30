package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.AnswerGenerationResultDto;
import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.dto.ChatCompletionRequestDto;
import com.milvus.vector_spring.llm.dto.ChatCompletionResponseDto;
import com.milvus.vector_spring.llm.dto.ConversationTurn;
import com.milvus.vector_spring.llm.provider.LlmProviderRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultChatCompletionService implements ChatCompletionService {

    private static final String DEFAULT_SYSTEM_INSTRUCTION =
            "You are a helpful assistant. Answer the user's question strictly based on the provided knowledge base. " +
            "If the knowledge base does not contain relevant information, state that clearly instead of guessing. " +
            "Always respond in the same language as the user's input.";

    private final LlmProviderRouter llmProviderRouter;

    /**
     * Decision tree:
     *  1. No vector candidates → no match (don't waste LLM tokens on empty context)
     *  2. No chat model configured → return top vector result directly
     *  3. Chat model configured + candidates present → synthesize with LLM over all candidates
     */
    @Override
    public AnswerGenerationResultDto generateAnswer(
            LlmPlatform platform,
            String chatModel, String userText, String apiKey,
            List<VectorSearchRankDto> candidates, String systemPrompt,
            long embeddingTokens, List<ConversationTurn> history
    ) {
        if (candidates.isEmpty()) {
            return AnswerGenerationResultDto.noMatch(embeddingTokens);
        }
        if (chatModel == null || chatModel.isBlank()) {
            return AnswerGenerationResultDto.fromVector(candidates.get(0).answer(), embeddingTokens);
        }
        return synthesizeWithLlm(platform, chatModel, userText, apiKey, candidates, systemPrompt, embeddingTokens, history);
    }

    private AnswerGenerationResultDto synthesizeWithLlm(
            LlmPlatform platform, String chatModel, String userText, String apiKey,
            List<VectorSearchRankDto> candidates, String systemPrompt, long embeddingTokens,
            List<ConversationTurn> history
    ) {
        try {
            String resolvedSystemMessage = buildSystemMessage(systemPrompt, candidates);
            ChatCompletionResponseDto response = llmProviderRouter.chat(
                    ChatCompletionRequestDto.from(platform, apiKey, chatModel, userText, resolvedSystemMessage, history)
            );
            return AnswerGenerationResultDto.fromLlm(
                    response.content(), embeddingTokens + response.totalTokens()
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ChatCompletion] LLM synthesis failed: {}", e.getMessage(), e);
            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
        }
    }

    /**
     * Builds the system message sent to the LLM.
     * Always injects RAG context so the LLM answers are grounded in knowledge base content.
     * Custom project prompt (if set) customizes behavior while still receiving the context.
     */
    private String buildSystemMessage(String customSystemPrompt, List<VectorSearchRankDto> candidates) {
        String baseInstruction = (customSystemPrompt != null && !customSystemPrompt.isBlank())
                ? customSystemPrompt.strip()
                : DEFAULT_SYSTEM_INSTRUCTION;

        return baseInstruction + "\n\nRelevant knowledge base:\n" + buildRagContext(candidates);
    }

    private String buildRagContext(List<VectorSearchRankDto> candidates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            VectorSearchRankDto c = candidates.get(i);
            sb.append("[%d] Topic: %s%n    Information: %s%n%n"
                    .formatted(i + 1, c.title(), c.answer()));
        }
        return sb.toString().strip();
    }
}
