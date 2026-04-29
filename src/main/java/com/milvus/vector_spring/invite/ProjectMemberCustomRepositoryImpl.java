package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;
import com.milvus.vector_spring.invite.dto.QCombinedProjectListResponseDto;
import com.milvus.vector_spring.project.QProject;
import com.milvus.vector_spring.user.QUser;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

public class ProjectMemberCustomRepositoryImpl implements ProjectMemberCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ProjectMemberCustomRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<CombinedProjectListResponseDto> findInvitedProjectsAsDto(Long userId) {
        QProjectMember member = QProjectMember.projectMember;
        QProject project = QProject.project;
        QUser user = QUser.user;

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
                .from(member)
                .join(member.project, project)
                .join(member.member, user)
                .where(user.id.eq(userId))
                .fetch();
    }
}
