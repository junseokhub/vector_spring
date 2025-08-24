package com.milvus.vector_spring.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository{
    private final JPAQueryFactory queryFactory;

    public UserCustomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public User findOneUserWithProjects(Long userId) {
        QUser user = QUser.user;
        User userResult = queryFactory
                .selectFrom(user)
                .where(user.id.eq(userId))
                .fetchOne();

        if (userResult == null) {
            return null;
        }
        return userResult;
    }
}
