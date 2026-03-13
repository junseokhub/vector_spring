package com.milvus.vector_spring.project;

import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;
import com.milvus.vector_spring.invite.dto.QCombinedProjectListResponseDto;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import java.util.Optional;

public class ProjectCustomRepositoryImpl implements ProjectCustomRepository {
    private final JPAQueryFactory queryFactory;

    public ProjectCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.queryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<Project> findOneProjectWithContents(String projectKey) {
        QProject project = QProject.project;
        Project projectResult = queryFactory
                .selectFrom(project)
                .where(project.key.eq(projectKey))
                .fetchOne();

        return Optional.ofNullable(projectResult);
    }

    @Override
    public List<CombinedProjectListResponseDto> findMyProjectsAsDto(Long userId) {
        QProject project = QProject.project;

        return queryFactory
                .select(new QCombinedProjectListResponseDto(
                        project.id,
                        Expressions.asBoolean(true).as("mine"), // 내 프로젝트이므로 true
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
