package com.milvus.vector_spring.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Modifying
    @Query("DELETE FROM Content c WHERE c.createdBy.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    Optional<Content> findOneContentByKey(String key);

    @Query("SELECT c FROM Content c WHERE c.project.key = :projectKey")
    List<Content> findByProjectKey(@Param("projectKey") String projectKey);

}
