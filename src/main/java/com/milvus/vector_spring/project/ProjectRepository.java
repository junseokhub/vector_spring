package com.milvus.vector_spring.project;

import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;
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

    @Query("SELECT p FROM Project p JOIN FETCH p.createdBy WHERE p.createdBy.id = :userId")
    List<Project> findAllByCreatedBy(@Param("userId") Long userId);
    // dto projection
    @Query("SELECT new com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto(p, true) " +
            "FROM Project p WHERE p.createdBy = :userId")
    List<CombinedProjectListResponseDto> findMyProjectsAsDto(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.key = :key")
    Project findProjectByKeyForUpdate(@Param("key") String key);
}
