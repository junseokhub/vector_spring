package com.milvus.vector_spring.llm;

import com.milvus.vector_spring.llm.dto.ModelListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/model")
public class LlmModelController {

    private final LlmModelRegistry modelRegistry;

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<ModelListResponseDto> listModels(
            @RequestParam(required = false) LlmPlatform platform,
            @RequestParam(required = false) ModelType type
    ) {
        List<LlmModelInfo> models;

        if (platform != null && type != null) {
            models = modelRegistry.findByPlatformAndType(platform, type);
        } else if (platform != null) {
            models = modelRegistry.findByPlatform(platform);
        } else if (type != null) {
            models = modelRegistry.findByType(type);
        } else {
            models = modelRegistry.findAll();
        }

        return models.stream()
                .map(ModelListResponseDto::from)
                .toList();
    }
}
