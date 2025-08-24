package com.milvus.vector_spring.project;

import com.querydsl.jpa.impl.JPAQueryFactory;

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
}
