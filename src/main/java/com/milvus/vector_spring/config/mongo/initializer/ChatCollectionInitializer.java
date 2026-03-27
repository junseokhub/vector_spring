    package com.milvus.vector_spring.config.mongo.initializer;

    import com.mongodb.client.MongoDatabase;
    import com.mongodb.client.model.ValidationOptions;
    import lombok.extern.slf4j.Slf4j;
    import org.bson.Document;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.data.mongodb.core.MongoTemplate;

    import java.util.Arrays;

    @Configuration
    @Slf4j
    public class ChatCollectionInitializer {

        @Bean
        public CommandLineRunner initChatCollection(MongoTemplate mongoTemplate) {
            return args -> {
                String collectionName = "chat_response";

                if (!mongoTemplate.collectionExists(collectionName)) {
                    Document jsonSchema = new Document()
                            .append("bsonType", "object")
                            .append("required", Arrays.asList("sessionId", "input", "output"))
                            .append("properties", new Document()
                                    .append("sessionId", new Document("bsonType", "string"))
                                    .append("input", new Document("bsonType", "string"))
                                    .append("output", new Document("bsonType", "string"))
                                    .append("vectorOutput", new Document("bsonType", "string"))
                                    .append("inputDateTime", new Document("bsonType", "date"))
                                    .append("outputDateTime", new Document("bsonType", "date"))
                                    .append("content", new Document("bsonType", "object"))
                                    .append("rank", new Document("bsonType", "array"))
                            );

                    ValidationOptions validationOptions = new ValidationOptions()
                            .validator(new Document("$jsonSchema", jsonSchema));


                    MongoDatabase db = mongoTemplate.getDb();
                    db.createCollection(collectionName, new com.mongodb.client.model.CreateCollectionOptions()
                            .validationOptions(validationOptions));
                    log.info("chat_response 컬렉션이 JSON 스키마와 함께 생성되었습니다.");
                } else {
                    log.info("chat_response 컬렉션이 이미 존재합니다.");
                }
            };
//
//            return args -> {
//                String schema = """
//                {
//                  "bsonType": "object",
//                  "required": ["sessionId", "projectKey", "input", "output", "vectorOutput", "inputDateTime", "outputDateTime", "rank"],
//                  "properties": {
//                    "sessionId": { "bsonType": "string" },
//                    "projectKey": { "bsonType": "string" },
//                    "input": { "bsonType": "string" },
//                    "output": { "bsonType": "string" },
//                    "vectorOutput": { "bsonType": "string" },
//                    "content": { "bsonType": "object" },
//                    "inputDateTime": { "bsonType": "date" },
//                    "outputDateTime": { "bsonType": "date" },
//                    "rank": {
//                      "bsonType": "array",
//                      "items": {
//                        "bsonType": "object",
//                        "properties": {
//                          "answer": { "bsonType": "string" },
//                          "title": { "bsonType": "string" },
//                          "score": { "bsonType": "double" },
//                          "id": { "bsonType": "int" }
//                        }
//                      }
//                    }
//                  }
//                }
//                """;
//
//                mongoTemplate.getDb().runCommand(new org.bson.Document("create", "chat_response")
//                        .append("validator", new org.bson.Document("$jsonSchema", org.bson.Document.parse(schema))));
//
//                System.out.println("chat_responses collection initialized.");
//            };
        }
    }
