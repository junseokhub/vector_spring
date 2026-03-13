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

//    @Override
//    public List<CombinedProjectListResponseDto> findCombinedProjects(User user) {
//
//        List<CombinedProjectListResponseDto> myProjects = queryFactory
//                .select(new QCombinedProjectListResponseDto(project, Expressions.asBoolean(true)))
//                .from(project)
//                .where(project.createdBy.eq(user.getId()))
//                .fetch();
//
//        List<CombinedProjectListResponseDto> invitedProjects = queryFactory
//                .select(new QCombinedProjectListResponseDto(invite.project, Expressions.asBoolean(false)))
//                .from(invite)
//                .join(invite.project, project)
//                .where(invite.receivedEmail.eq(user.getEmail()))
//                .fetch();
//
//        myProjects.addAll(invitedProjects);
//        return myProjects;
//    }
}
