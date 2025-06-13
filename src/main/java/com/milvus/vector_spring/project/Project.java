package com.milvus.vector_spring.project;

import com.milvus.vector_spring.common.BaseEntity;
import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.invite.Invite;
import com.milvus.vector_spring.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "open_ai_key", nullable = true, length = 1024)
    private String openAiKey;

    @Column(name = "prompt", nullable = true)
    private String prompt;

    @Column(name = "chat_model", nullable = true)
    private String chatModel;

    @Column(name = "embed_model", nullable = true)
    private String embedModel;

    @Column(name = "dimensions", nullable = true)
    private long dimensions;

    @Column(name = "total_token")
    private long totalToken;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invite> invites = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_user_id", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_user_id", nullable = true)
    private User updatedBy;

    @Builder
    public Project(Long id, String name, String key, String openAiKey, String prompt, String chatModel, String embedModel, long dimensions, long totalToken, LocalDateTime createdAt, LocalDateTime updatedAt, User createdBy, User updatedBy) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.openAiKey = openAiKey;
        this.prompt = prompt;
        this.chatModel = chatModel;
        this.embedModel = embedModel;
        this.dimensions = dimensions;
        this.totalToken = totalToken;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public void updateByUser(User updatedUser, User createdUser) {
        this.updatedBy = updatedUser;
        this.createdBy = createdUser;
    }

    public void updateTotalToken(long totalToken) {
        this.totalToken = totalToken;
    }
}
