package com.milvus.vector_spring.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentChunkRepository extends JpaRepository<ContentChunk, Long> {
    List<ContentChunk> findByContent(Content content);
    void deleteByContent(Content content);
}
