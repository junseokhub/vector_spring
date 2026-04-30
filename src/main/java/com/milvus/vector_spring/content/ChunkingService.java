package com.milvus.vector_spring.content;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits long text into overlapping chunks for finer-grained vector indexing.
 * Shorter texts are returned as a single chunk without splitting.
 */
@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;

    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        text = text.strip();
        if (text.length() <= CHUNK_SIZE) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) break;
            start = end - OVERLAP;
        }
        return chunks;
    }
}
