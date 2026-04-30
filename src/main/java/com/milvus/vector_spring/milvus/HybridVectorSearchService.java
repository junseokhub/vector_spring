package com.milvus.vector_spring.milvus;

import com.milvus.vector_spring.chat.dto.VectorSearchRankDto;
import com.milvus.vector_spring.chat.dto.VectorSearchResponseDto;
import com.milvus.vector_spring.config.properties.MilvusProperties;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Hybrid vector search: dense (COSINE) + sparse TF keyword search (IP) fused via RRF.
 * Active only when milvus.search-mode=HYBRID.
 * Requires collections created by DefaultHybridMilvusService (includes sparse_vector field).
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "milvus.search-mode", havingValue = "HYBRID")
public class HybridVectorSearchService implements VectorSearchService {

    private final DefaultHybridMilvusService hybridMilvusService;
    private final MilvusProperties milvusProperties;

    @Override
    public VectorSearchResponseDto search(List<Float> embedding, String queryText, Long projectId) {
        SearchResp searchResp = hybridMilvusService.hybridSearch(embedding, queryText, projectId);
        double threshold = milvusProperties.scoreThreshold();

        List<VectorSearchRankDto> results = searchResp.getSearchResults().stream()
                .flatMap(List::stream)
                .filter(result -> result.getScore() >= threshold)
                .map(result -> {
                    Map<String, Object> entity = result.getEntity();
                    return new VectorSearchRankDto(
                            ((Number) result.getId()).longValue(),
                            (String) entity.get("title"),
                            (String) entity.get("answer"),
                            result.getScore()
                    );
                })
                .toList();

        Long firstId = results.isEmpty() ? null : results.get(0).id();
        return new VectorSearchResponseDto(firstId, results);
    }
}
