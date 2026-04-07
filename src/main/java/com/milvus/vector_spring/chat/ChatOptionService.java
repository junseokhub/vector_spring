package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;
import com.milvus.vector_spring.milvus.MilvusService;
import com.milvus.vector_spring.openai.OpenAiService;
import com.milvus.vector_spring.openai.dto.OpenAiChatRequestDto;
import com.milvus.vector_spring.openai.dto.OpenAiChatResponseDto;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatOptionService {
    private final OpenAiService openAiService;
    private final MilvusService milvusService;

    public String prompt(String text, List<String> data) {
        return """
            You are a chatbot designed to understand user questions accurately and provide helpful responses.
            Please follow the steps below in order, but do not include step 1 in your response.
            1. Analyze the user utterance "%s" to determine the user's question intent.
            2. If there is relevant information, use it to provide an answer.
            3. If there is no relevant information, inform the user and ask them to try another question.
            4. You must answer in korean and only user's answer
            %s
        """.formatted(text, data);
    }

    public Mono<OpenAiChatResponseDto> openAiChatResponse(String openAiKey, String prompt, String model) {
        List<OpenAiChatRequestDto.OpenAiMessageDto> messages = List.of(
                new OpenAiChatRequestDto.OpenAiMessageDto("system", prompt)
        );
        OpenAiChatRequestDto requestDto = new OpenAiChatRequestDto(model, messages);
        return openAiService.chat(openAiKey, requestDto);
    }

    public Mono<OpenAiChatResponseDto> onlyOpenAiAnswer(String openAiKey, String question, String model) {
        List<OpenAiChatRequestDto.OpenAiMessageDto> messages = List.of(
                new OpenAiChatRequestDto.OpenAiMessageDto("user", question)
        );
        OpenAiChatRequestDto requestDto = new OpenAiChatRequestDto(model, messages);
        return openAiService.chat(openAiKey, requestDto);
    }

    public VectorSearchResponseDto vectorSearchResult(List<Float> vector, Long dbKey) {
        SearchResp search = milvusService.vectorSearch(vector, dbKey);
        Long id = (Long) search.getSearchResults().stream()
                .findFirst()
                .flatMap(results -> results.stream().findFirst())
                .map(SearchResp.SearchResult::getId)
                .orElse(null);

        List<String> answers = search.getSearchResults().stream()
                .flatMap(Collection::stream)
                .map(result -> (String) result.getEntity().get("answer"))
                .toList();

        return new VectorSearchResponseDto(search, id, answers);
    }
}
