package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;
import com.milvus.vector_spring.invite.dto.QCombinedProjectListResponseDto;
import com.milvus.vector_spring.project.QProject;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

public class InviteCustomRepositoryImpl implements InviteCustomRepository {
    private final JPAQueryFactory queryFactory;

    public InviteCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<CombinedProjectListResponseDto> findInvitedProjectsAsDto(String email) {
        QInvite invite = QInvite.invite;
        QProject project = QProject.project;

        return queryFactory
                .select(new QCombinedProjectListResponseDto(
                        project.id,
                        Expressions.asBoolean(false).as("mine"),
                        project.name,
                        project.key,
                        project.createdBy.id,
                        project.updatedBy.id,
                        project.createdAt,
                        project.updatedAt
                ))
                .from(invite)
                .join(invite.project, project)
                .where(invite.receivedEmail.eq(email))
                .fetch();
    }
}
