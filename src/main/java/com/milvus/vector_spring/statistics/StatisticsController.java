package com.milvus.vector_spring.statistics;

import com.milvus.vector_spring.statistics.dto.MongoChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/{projectKey}")
    @ResponseStatus(HttpStatus.OK)
    public List<MongoChatResponse> getByProjectKey(@RequestParam("projectKey") String projectKey) {
        return statisticsService.findByProjectKey(projectKey);
    }

    @GetMapping("/sessionId")
    @ResponseStatus(HttpStatus.OK)
    public List<MongoChatResponse> getBySessionId(
            @RequestParam("projectKey") String projectKey,
            @RequestParam("sessionId") String sessionId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ){
        return statisticsService.findByProjectKeyAndSessionId(projectKey, sessionId, startDate, endDate);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<MongoChatResponse> getAllLogs() {
        return statisticsService.findAllLog();
    }
}
