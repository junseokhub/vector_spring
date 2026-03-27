package com.milvus.vector_spring.statistics;

import com.milvus.vector_spring.statistics.dto.MongoChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StatisticsService {
    private final MongoTemplate mongoTemplate;

    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    public List<MongoChatResponse> findByProjectKey(String projectKey) {
        Query query = new Query(Criteria.where("projectKey").is(projectKey));
        return mongoTemplate.find(query, MongoChatResponse.class);
    }

    public List<MongoChatResponse> findByProjectKeyAndSessionId(
            String projectKey,
            String sessionId,
            String startDate,
            String endDate
    ) {
        Date startKst = convertKstStringToUtc(startDate);
        Date endKst = convertKstStringToUtc(endDate);

        Query query = new Query(
                new Criteria().andOperator(
                        Criteria.where("projectKey").is(projectKey),
                        Criteria.where("sessionId").is(sessionId),
                        Criteria.where("inputDateTime").gte(startKst).lte(endKst)
                )
        );

        List<MongoChatResponse> results = mongoTemplate.find(query, MongoChatResponse.class);

        return results.stream()
                .map(this::convertToKst)
                .collect(Collectors.toList());
    }

    public List<MongoChatResponse> findAllLog() {
        return mongoTemplate.findAll(MongoChatResponse.class);
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Date convertKstStringToUtc(String dateTimeStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, FORMATTER);

        ZonedDateTime kstZoned = localDateTime.atZone(KST_ZONE_ID);
        ZonedDateTime utcZoned = kstZoned.withZoneSameInstant(UTC_ZONE_ID);

        return Date.from(utcZoned.toInstant());
    }

    private Date convertUtcToKst(Date utcDate) {
        ZonedDateTime utcZonedDateTime = utcDate.toInstant().atZone(UTC_ZONE_ID);
        ZonedDateTime kstZonedDateTime = utcZonedDateTime.withZoneSameInstant(KST_ZONE_ID);
        return Date.from(kstZonedDateTime.toInstant());
    }

    private MongoChatResponse convertToKst(MongoChatResponse response) {
        if (response.getInputDateTime() != null) {
            response.setInputDateTime(convertUtcToKst(response.getInputDateTime()));
        }
        if (response.getOutputDateTime() != null) {
            response.setOutputDateTime(convertUtcToKst(response.getOutputDateTime()));
        }
        return response;
    }
}
