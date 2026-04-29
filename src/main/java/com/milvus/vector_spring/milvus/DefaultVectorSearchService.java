package com.milvus.vector_spring.milvus;

import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.properties.MilvusProperties;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DefaultVectorSearchService implements VectorSearchService {

    private final MilvusService milvusService;
    private final MilvusProperties milvusProperties;

    @Override
    public VectorSearchResponseDto search(List<Float> embedding, Long projectId) {
        try {
            SearchResp searchResp = milvusService.vectorSearch(embedding, projectId);
            double threshold = milvusProperties.scoreThreshold();

            List<VectorSearchRankDto> results = searchResp.getSearchResults().stream()
                    .flatMap(List::stream)
                    .filter(result -> result.getScore() >= threshold)
                    .map(result -> {
                        Map<String, Object> entity = result.getEntity();
                        return new VectorSearchRankDto(
                                (Long) result.getId(),
                                (String) entity.get("title"),
                                (String) entity.get("answer"),
                                result.getScore()
                        );
                    })
                    .toList();

            Long firstId = results.isEmpty() ? null : results.get(0).id();

            return new VectorSearchResponseDto(firstId, results);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_VECTOR_SEARCH_ERROR);
        }
    }
}
