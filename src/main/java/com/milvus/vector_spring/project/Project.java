package com.milvus.vector_spring.project;

import com.milvus.vector_spring.common.BaseEntity;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "project")
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "project_key", nullable = false, unique = true)
    private String key;

    @Column(name = "open_ai_key", length = 1024)
    private String apiKey;

    @Column(name = "prompt")
    private String prompt;

    @Column(name = "chat_model")
    private String chatModel;

    @Column(name = "embed_model")
    private String embedModel;

    @Column(name = "dimensions")
    private long dimensions;

    @Column(name = "total_token")
    private long totalToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "llm_platform")
    private LlmPlatform llmPlatform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_user_id")
    private User updatedBy;

    @Builder
    public Project(Long id, String name, String key, String apiKey, String prompt,
                   String chatModel, String embedModel, long dimensions, long totalToken,
                   LlmPlatform llmPlatform,
                   LocalDateTime createdAt, LocalDateTime updatedAt, User createdBy, User updatedBy) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.apiKey = apiKey;
        this.prompt = prompt;
        this.chatModel = chatModel;
        this.embedModel = embedModel;
        this.dimensions = dimensions;
        this.totalToken = totalToken;
        this.llmPlatform = llmPlatform;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public void update(String name, String apiKey, String chatModel, String embedModel,
                       String prompt, LlmPlatform llmPlatform, User updatedBy) {
        if (name != null && !name.isBlank()) this.name = name;
        if (apiKey != null) this.apiKey = apiKey;
        if (chatModel != null && !chatModel.isBlank()) this.chatModel = chatModel;
        if (embedModel != null) this.embedModel = embedModel;
        if (prompt != null) this.prompt = prompt;
        if (llmPlatform != null) this.llmPlatform = llmPlatform;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void transferOwnership(User newOwner) {
        this.createdBy = newOwner;
        this.updatedBy = newOwner;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTotalToken(long totalToken) {
        this.totalToken = totalToken;
    }
}
