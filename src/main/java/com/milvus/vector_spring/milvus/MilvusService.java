package com.milvus.vector_spring.milvus;

import com.milvus.vector_spring.milvus.dto.InsertRequestDto;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.List;

public interface MilvusService {
    void createSchema(Long dbKey, int dimension);

    boolean checkCollectionLoadState(Long dbKey);

    void upsertCollection(long id, InsertRequestDto insertRequestDto, Long dbKey);

    void deleteCollection(long id);

    boolean hasCollection();

    SearchResp vectorSearch(List<Float> vectorData, Long dbKey);
}
