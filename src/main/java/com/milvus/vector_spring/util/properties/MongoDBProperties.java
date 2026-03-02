package com.milvus.vector_spring.util.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;

@ConfigurationProperties(prefix = "spring.data.mongodb")
public record MongoDBProperties(
        String database,
        String uri,
        String authenticationDatabase,
        boolean autoIndexCreation
) {
}
//package com.milvus.vector_spring.config.properties;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//@Getter
//@Setter
//@Component
//@ConfigurationProperties(prefix = "spring.data.mongodb")
//public class MongoDBProperties {
//
//    private String database;
//    private String uri;
//    private String authenticationDatabase;
//    private boolean autoIndexCreation;
//}