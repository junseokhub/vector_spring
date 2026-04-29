package com.milvus.vector_spring.config;

import com.milvus.vector_spring.config.properties.MilvusProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    @Bean()
    public MilvusClientV2 milvusClientV2(MilvusProperties milvusProperties) {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(milvusProperties.clusterEndpoint())
                .token(milvusProperties.token())
                .build();

        return new MilvusClientV2(connectConfig);
    }
}
