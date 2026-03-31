package com.milvus.vector_spring.ollama;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/embed")
public class OllamaController {

    private final OllamaService ollamaService;

    @PostMapping
    public ResponseEntity<float[]> embed(@RequestBody String text) {
        return ResponseEntity.ok(ollamaService.embed(text));
    }
}