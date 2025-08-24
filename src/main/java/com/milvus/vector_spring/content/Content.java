package com.milvus.vector_spring.content;

import com.milvus.vector_spring.common.BaseEntity;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "content")
public class Content extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "content_key", nullable = false, unique = true)
    private String key;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "answer", nullable = false)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_user_id")
    private User updatedBy;

    @Builder
    public Content(Long id, String key, String title, String answer, Project project, LocalDateTime createdAt, LocalDateTime updatedAt, User createdBy, User updatedBy) {
        this.id = id;
        this.key = key;
        this.title = title;
        this.answer = answer;
        this.project = project;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
