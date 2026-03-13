package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    Optional<Invite> findByProjectAndReceivedEmail(Project project, String receivedEmail);

    @Query("SELECT i FROM Invite i JOIN FETCH i.project p JOIN FETCH p.createdBy WHERE i.receivedEmail = :receivedEmail")
    List<Invite> findAllByReceivedEmailWithProject(@Param("receivedEmail") String receivedEmail);

    @Query("SELECT i FROM Invite i JOIN FETCH i.project WHERE i.project = :project")
    List<Invite> findAllByProject(@Param("project") Project project);

    @Query("SELECT i FROM Invite i JOIN FETCH i.project WHERE i.createdBy = :createdBy AND i.project = :project")
    List<Invite> findAllByCreatedByAndProject(@Param("createdBy") User createdBy, @Param("project") Project project);

    @Modifying
    @Query("DELETE FROM Invite c WHERE c.createdBy.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Invite i " +
            "JOIN FETCH i.project p " +
            "JOIN FETCH p.createdBy " +
            "WHERE p.key = :projectKey")
    List<Invite> findAllByProjectKeyWithDetails(@Param("projectKey") String projectKey);


    // 서버 과부화 예방용
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Invite i WHERE i.createdBy = :beforeMaster AND i.project = :project AND i.receivedEmail = :email")
    void deleteByCreatedByAndProjectAndReceivedEmail(@Param("beforeMaster") User beforeMaster,
                                                     @Param("project") Project project,
                                                     @Param("email") String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Invite i SET i.createdBy = :afterMaster WHERE i.createdBy = :beforeMaster AND i.project = :project AND i.receivedEmail <> :email")
    void updateCreatedByForProject(@Param("beforeMaster") User beforeMaster,
                                   @Param("afterMaster") User afterMaster,
                                   @Param("project") Project project,
                                   @Param("email") String email);
}