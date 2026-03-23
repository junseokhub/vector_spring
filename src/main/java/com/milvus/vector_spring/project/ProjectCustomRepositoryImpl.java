package com.milvus.vector_spring.project;

import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;
import com.milvus.vector_spring.invite.dto.QCombinedProjectListResponseDto;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

public class ProjectCustomRepositoryImpl implements ProjectCustomRepository {
    private final JPAQueryFactory queryFactory;

    public ProjectCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.queryFactory = jpaQueryFactory;
    }

    @Override
    public Project findOneProjectWithContents(String projectKey) {
        QProject project = QProject.project;

        return queryFactory
                .selectFrom(project)
                .where(project.key.eq(projectKey))
                .fetchOne();
    }

    @Override
    public List<CombinedProjectListResponseDto> findMyProjectsAsDto(Long userId) {
        QProject project = QProject.project;

        return queryFactory
                .select(new QCombinedProjectListResponseDto(
                        project.id,
                        Expressions.asBoolean(true).as("mine"),
                        project.name,
                        project.key,
                        project.createdBy.id,
                        project.updatedBy.id,
                        project.createdAt,
                        project.updatedAt
                ))
                .from(project)
                .where(project.createdBy.id.eq(userId))
                .fetch();
    }
}
