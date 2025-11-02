package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.AnswerGenerationResultDto;
import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.libraryopenai.OpenAiLibraryService;
import com.milvus.vector_spring.libraryopenai.dto.OpenAiChatLibraryRequestDto;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.completions.CompletionUsage;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatCompletionServiceImpl implements ChatCompletionService {

    private final ChatOptionService chatOptionService;
    private final OpenAiLibraryService openAiLibraryService;

    @Override
    public AnswerGenerationResultDto generateAnswerWithDecision(
            String chatModel,
            String userText,
            String openAiKey,
            List<VectorSearchRankDto> rankList,
            VectorSearchResponseDto searchResponse,
            String prompt,
            CreateEmbeddingResponse embeddingResponse
    ) {
        String finalAnswer;
        long totalToken;

        if (rankList.isEmpty() || rankList.get(0).getScore() < 0.5) {
            try {
                OpenAiChatLibraryRequestDto dto = OpenAiChatLibraryRequestDto.builder()
                        .model(chatModel)
                        .openAiKey(openAiKey)
                        .userMessages(userText)
                        .systemMesasges(
                                (prompt == null || prompt.isEmpty())
                                        ? chatOptionService.prompt(userText, rankList.stream().map(VectorSearchRankDto::getAnswer).toList())
                                        : prompt
                        )
                        .build();

                ChatCompletion answer = openAiLibraryService.chat(dto);

                finalAnswer = answer.choices().get(0).message().content().orElse("");
                totalToken = embeddingResponse.usage().totalTokens() +
                        answer.usage().stream()
                                .mapToLong(CompletionUsage::totalTokens)
                                .sum();
            } catch (Exception e) {
                throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
            }
        } else {
            totalToken = embeddingResponse.usage().totalTokens();
            finalAnswer = rankList.get(0).getAnswer();
        }

        return new AnswerGenerationResultDto(finalAnswer, totalToken, LocalDateTime.now());
    }

//    @Override
//    public ChatCompletion generateAnswer(String chatModel, String userText, String openAiKey, List<VectorSearchRankDto> rankList, VectorSearchResponseDto searchResponse, String prompt) {
//        try {
//            OpenAiChatLibraryRequestDto dto = OpenAiChatLibraryRequestDto.builder()
//                    .model(chatModel)
//                    .openAiKey(openAiKey)
//                    .userMessages(userText)
//                    .systemMesasges(
//                            (prompt == null || prompt.isEmpty())
//                                    ? chatOptionService.prompt(userText, rankList.stream().map(VectorSearchRankDto::getAnswer).toList())
//                                    : prompt
//                    )
//                    .build();
//
//            return openAiLibraryService.chat(dto);
//
//        } catch (Exception e) {
//            throw new CustomException(ErrorStatus.OPEN_AI_ERROR);
//        }
//    }
}
