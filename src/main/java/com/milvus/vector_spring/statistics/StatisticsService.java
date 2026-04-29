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

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final MongoTemplate mongoTemplate;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<MongoChatResponse> findByProjectKey(String projectKey) {
        return mongoTemplate.find(
                new Query(Criteria.where("projectKey").is(projectKey)),
                MongoChatResponse.class
        );
    }

    public List<MongoChatResponse> findByProjectKeyAndSessionId(
            String projectKey, String sessionId, String startDate, String endDate
    ) {
        Date start = toUtcDate(startDate);
        Date end = toUtcDate(endDate);

        Query query = new Query(new Criteria().andOperator(
                Criteria.where("projectKey").is(projectKey),
                Criteria.where("sessionId").is(sessionId),
                Criteria.where("inputDateTime").gte(start).lte(end)
        ));

        return mongoTemplate.find(query, MongoChatResponse.class).stream()
                .map(this::toKst)
                .toList();
    }

    public List<MongoChatResponse> findAllLog() {
        return mongoTemplate.findAll(MongoChatResponse.class);
    }

    private Date toUtcDate(String kstDateStr) {
        LocalDateTime local = LocalDateTime.parse(kstDateStr, DATE_FORMATTER);
        return Date.from(local.atZone(KST).withZoneSameInstant(UTC).toInstant());
    }

    private MongoChatResponse toKst(MongoChatResponse response) {
        Date kstInput = response.getInputDateTime() != null
                ? toKstDate(response.getInputDateTime()) : null;
        Date kstOutput = response.getOutputDateTime() != null
                ? toKstDate(response.getOutputDateTime()) : null;
        response.setInputDateTime(kstInput);
        response.setOutputDateTime(kstOutput);
        return response;
    }

    private Date toKstDate(Date utcDate) {
        ZonedDateTime utcZdt = utcDate.toInstant().atZone(UTC);
        return Date.from(utcZdt.withZoneSameInstant(KST).toInstant());
    }
}
