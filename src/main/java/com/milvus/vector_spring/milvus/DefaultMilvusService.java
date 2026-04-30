package com.milvus.vector_spring.milvus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.properties.MilvusProperties;
import com.milvus.vector_spring.milvus.dto.InsertRequestDto;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.rbac.request.CreateUserReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "milvus.search-mode", havingValue = "DENSE", matchIfMissing = true)
public class DefaultMilvusService implements MilvusService {

    private final MilvusProperties milvusProperties;
    private final MilvusClientV2 client;

    @Override
    public void createSchema(Long dbKey, int dimension) {
        try {
            List<String> existingCollections = client.listCollections().getCollectionNames();
            if (existingCollections.contains(collectionName(dbKey))) {
                throw new CustomException(ErrorStatus.MILVUS_COLLECTION_ALREADY_EXISTS);
            }

            List<String> users = client.listUsers();
            if (!users.contains(milvusProperties.username())) {
                client.createUser(CreateUserReq.builder()
                        .userName(milvusProperties.username())
                        .password(milvusProperties.password())
                        .build());
            }

            CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder().build();
            schema.addField(AddFieldReq.builder().fieldName("id").dataType(DataType.Int64).isPrimaryKey(true).autoID(false).build());
            schema.addField(AddFieldReq.builder().fieldName("vector").dataType(DataType.FloatVector).dimension(dimension).build());
            schema.addField(AddFieldReq.builder().fieldName("title").dataType(DataType.VarChar).maxLength(512).build());
            schema.addField(AddFieldReq.builder().fieldName("answer").dataType(DataType.VarChar).maxLength(65535).build());

            client.createCollection(CreateCollectionReq.builder()
                    .collectionName(collectionName(dbKey))
                    .collectionSchema(schema)
                    .indexParams(buildHnswIndex())
                    .build());

            client.getLoadState(GetLoadStateReq.builder().collectionName(collectionName(dbKey)).build());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_SCHEMA_CREATE_ERROR);
        }
    }

    @Override
    public boolean checkCollectionLoadState(Long dbKey) {
        Boolean loaded = client.getLoadState(
                GetLoadStateReq.builder().collectionName(collectionName(dbKey)).build()
        );
        log.info("Collection load state: {}", loaded);
        return Boolean.TRUE.equals(loaded);
    }

    @Override
    public void upsertCollection(long id, InsertRequestDto insertRequestDto, Long dbKey) {
        try {
            JsonObject dataObject = new JsonObject();
            JsonArray vectorArray = new JsonArray();
            insertRequestDto.vector().forEach(vectorArray::add);
            dataObject.addProperty("id", id);
            dataObject.add("vector", vectorArray);
            dataObject.addProperty("title", insertRequestDto.title());
            dataObject.addProperty("answer", insertRequestDto.answer());

            client.upsert(UpsertReq.builder()
                    .collectionName(collectionName(dbKey))
                    .data(List.of(dataObject))
                    .build());
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_UPSERT_ERROR);
        }
    }

    @Override
    public void deleteCollection(long id) {
        try {
            client.dropCollection(DropCollectionReq.builder()
                    .collectionName(collectionName(id))
                    .build());
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_DELETE_ERROR);
        }
    }

    @Override
    public boolean hasCollection() {
        return client.hasCollection(HasCollectionReq.builder()
                .collectionName(milvusProperties.collectionName())
                .build());
    }

    @Override
    public SearchResp vectorSearch(List<Float> vectorData, Long dbKey) {
        try {
            List<BaseVector> baseVectors = new ArrayList<>();
            if (vectorData != null) {
                baseVectors.add(new FloatVec(new ArrayList<>(vectorData)));
            }
            return client.search(SearchReq.builder()
                    .collectionName(collectionName(dbKey))
                    .data(baseVectors)
                    .limit(milvusProperties.topK())
                    .searchParams(Map.of("metric_type", "COSINE", "ef", 300))
                    .outputFields(Arrays.asList("title", "answer"))
                    .build());
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_VECTOR_SEARCH_ERROR);
        }
    }

    private List<IndexParam> buildHnswIndex() {
        return List.of(IndexParam.builder()
                .fieldName("vector")
                .indexType(IndexParam.IndexType.HNSW)
                .metricType(IndexParam.MetricType.COSINE)
                .extraParams(Map.of("efConstruction", 300, "M", 32))
                .build());
    }

    private String collectionName(long dbKey) {
        return milvusProperties.collectionName() + dbKey;
    }
}
