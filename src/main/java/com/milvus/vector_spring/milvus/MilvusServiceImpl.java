package com.milvus.vector_spring.milvus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.util.properties.MilvusProperties;
import com.milvus.vector_spring.milvus.dto.InsertRequestDto;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.rbac.request.CreateUserReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MilvusServiceImpl implements MilvusService {

    private final MilvusProperties milvusProperties;
    private final MilvusClientV2 client;

    @Override
    public void createSchema(Long dbKey, int dimension) {
        try {
            List<String> existingCollections = client.listCollections().getCollectionNames();
            if (existingCollections.contains(milvusProperties.collectionName() + dbKey)) {
                throw new CustomException(ErrorStatus.MILVUS_COLLECTION_ALREADY_EXISTS);
            }

            CreateUserReq createUserReq = CreateUserReq.builder()
                    .userName(milvusProperties.username())
                    .password(milvusProperties.password())
                    .build();

            List<String> users = client.listUsers();
            if (!users.contains(milvusProperties.username())) {
                client.createUser(createUserReq);
            }

            CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder().build();
            schema.addField(AddFieldReq.builder()
                    .fieldName("id")
                    .dataType(DataType.Int64)
                    .isPrimaryKey(true)
                    .autoID(false)
                    .build());
            schema.addField(AddFieldReq.builder()
                    .fieldName("vector")
                    .dataType(DataType.FloatVector)
                    .dimension(dimension)
                    .build());
            schema.addField(AddFieldReq.builder()
                    .fieldName("title")
                    .dataType(DataType.VarChar)
                    .maxLength(128)
                    .build());
            schema.addField(AddFieldReq.builder()
                    .fieldName("answer")
                    .dataType(DataType.VarChar)
                    .maxLength(3092)
                    .build());

            List<IndexParam> indexParamList = createIndex();
            CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                    .collectionName(milvusProperties.collectionName() + dbKey)
                    .collectionSchema(schema)
                    .indexParams(indexParamList)
                    .build();
            client.createCollection(createCollectionReq);

            GetLoadStateReq getLoadStateReq = loadCollection(milvusProperties.collectionName() + dbKey);
            client.getLoadState(getLoadStateReq);

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_SCHEMA_CREATE_ERROR);
        }
    }

    private List<IndexParam> createIndex() {
        try {
            IndexParam indexParamForVectorField = IndexParam.builder()
                    .fieldName("vector")
                    .indexType(IndexParam.IndexType.HNSW)
                    .metricType(IndexParam.MetricType.COSINE)
                    .extraParams(Map.of("efConstruction", 300, "M", 32))
                    .build();

            return List.of(indexParamForVectorField);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_INDEX_CREATE_ERROR);
        }
    }

    private GetLoadStateReq loadCollection(String collectionName) {
        return GetLoadStateReq.builder()
                .collectionName(collectionName)
                .build();
    }

    @Override
    public boolean checkCollectionLoadState(Long dbKey) {
        GetLoadStateReq loadStateReq = GetLoadStateReq.builder()
                .collectionName(milvusProperties.collectionName() + dbKey)
                .build();
        Boolean res = client.getLoadState(loadStateReq);
        System.out.println("Collection load state: " + res);
        return res;
    }

    @Override
    public void upsertCollection(long id, InsertRequestDto insertRequestDto, Long dbKey) {
        JsonObject dataObject = new JsonObject();
        JsonArray vectorArray = new JsonArray();

        try {
            for (Float v : insertRequestDto.getVector()) {
                vectorArray.add(v);
            }
            dataObject.addProperty("id", id);
            dataObject.add("vector", vectorArray);
            dataObject.addProperty("title", insertRequestDto.getTitle());
            dataObject.addProperty("answer", insertRequestDto.getAnswer());

            List<JsonObject> data = List.of(dataObject);

            UpsertReq upsertReq = UpsertReq.builder()
                    .collectionName(milvusProperties.collectionName() + dbKey)
                    .data(data)
                    .build();
            client.upsert(upsertReq);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_UPSERT_ERROR);
        }
    }

    @Override
    public void deleteCollection(long id) {
        try {
            DeleteReq deleteReq = DeleteReq.builder()
                    .collectionName(milvusProperties.collectionName() + id)
                    .filter("id in [" + id + "]")
                    .build();
            client.delete(deleteReq);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_DELETE_ERROR);
        }
    }

    @Override
    public boolean hasCollection() {
        HasCollectionReq hasCollectionReq = HasCollectionReq.builder()
                .collectionName(milvusProperties.collectionName())
                .build();
        return client.hasCollection(hasCollectionReq);
    }

    public SearchResp vectorSearch(List<Float> vectorData, Long dbKey) {
        try {
            List<BaseVector> baseVectors = new ArrayList<>();
            if (vectorData != null) {
                List<Float> floatList = new ArrayList<>(vectorData);
                baseVectors.add(new FloatVec(floatList));
            }
            List<String> fields = Arrays.asList("title", "answer");
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(milvusProperties.collectionName() + dbKey)
                    .data(baseVectors)
                    .limit(5)
                    .searchParams(Map.of("metric_type", "COSINE", "ef", 300))
                    .outputFields(fields)
                    .build();

            return client.search(searchReq);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_VECTOR_SEARCH_ERROR);
        }
    }
}
