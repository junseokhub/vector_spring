package com.milvus.vector_spring.content;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_chunk")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    private String chunkText;

    public static ContentChunk of(Content content, int index, String text) {
        ContentChunk chunk = new ContentChunk();
        chunk.content = content;
        chunk.chunkIndex = index;
        chunk.chunkText = text;
        return chunk;
    }
}
