package com.milvus.vector_spring.project;

import com.milvus.vector_spring.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository  extends JpaRepository<Project, Long>, ProjectCustomRepository {
    Optional<Project> findProjectByKey(String key);

    @Modifying
    @Query("DELETE FROM Project c WHERE c.createdBy.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    List<Project> findAllByCreatedBy(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.key = :key")
    Project findProjectByKeyForUpdate(@Param("key") String key);
}
