package com.milvus.vector_spring.milvus;

import com.milvus.vector_spring.chat.ChatOptionService;
import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.libraryopenai.OpenAiLibraryService;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VectorSearchServiceImpl implements VectorSearchService {

    private final OpenAiLibraryService openAiLibraryService;
    private final ChatOptionService chatOptionService;

    @Override
    public CreateEmbeddingResponse createEmbedding(String openAiKey, String text, long dimensions) {
        return openAiLibraryService.embedding(openAiKey, text, dimensions);
    }

    @Override
    public VectorSearchResponseDto searchVector(CreateEmbeddingResponse embedding, Long projectId) {
        try {
            List<Float> floatList = embedding.data().get(0).embedding();
            return chatOptionService.vectorSearchResult(floatList, projectId);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.DATA_PARSE_ERROR);
        }
    }

    @Override
    public List<VectorSearchRankDto> convertToRankList(VectorSearchResponseDto searchResponse) {
        try {
            return searchResponse.getSearch().getSearchResults().stream()
                    .flatMap(List::stream)
                    .map(result -> {
                        Map<String, Object> entity = result.getEntity();
                        return VectorSearchRankDto.builder()
                                .answer((String) entity.get("answer"))
                                .title((String) entity.get("title"))
                                .score(result.getScore())
                                .id((Long) result.getId())
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.DATA_PARSE_ERROR);
        }
    }
}