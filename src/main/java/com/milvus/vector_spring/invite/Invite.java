package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.common.BaseEntity;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
@Table(name = "invite")
public class Invite extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "received_email")
    private String receivedEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder
    public Invite(Long id, String receivedEmail, User createdBy, Project project) {
        this.id = id;
        this.receivedEmail = receivedEmail;
        this.createdBy = createdBy;
        this.project = project;
    }

    public void updateCreatedBy(User newCreatedBy) {
        this.createdBy = newCreatedBy;
    }
}
