package com.milvus.vector_spring.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Modifying
    @Query("DELETE FROM Content c WHERE c.createdBy.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Content c WHERE c.key = :key")
    Content findOneContentByKey(@Param("key") String key);

    @Query("SELECT c FROM Content c JOIN FETCH c.project WHERE c.project.key = :projectKey")
    List<Content> findByProjectKey(@Param("projectKey") String projectKey);

    @Query("SELECT c FROM Content c JOIN FETCH c.project p JOIN FETCH c.createdBy u WHERE c.id = :id")
    Content findByIdWithProjectAndUser(@Param("id") Long id);
}