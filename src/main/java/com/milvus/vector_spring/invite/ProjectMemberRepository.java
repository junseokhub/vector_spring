package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long>, ProjectMemberCustomRepository {

    Optional<ProjectMember> findByProjectAndMember(Project project, User member);

    @Query("SELECT m FROM ProjectMember m JOIN FETCH m.project p JOIN FETCH p.createdBy WHERE m.member = :member")
    List<ProjectMember> findAllByMemberWithProject(@Param("member") User member);

    @Query("SELECT m FROM ProjectMember m JOIN FETCH m.project WHERE m.invitedBy = :invitedBy AND m.project = :project")
    List<ProjectMember> findAllByInvitedByAndProject(@Param("invitedBy") User invitedBy, @Param("project") Project project);

    @Modifying
    @Query("DELETE FROM ProjectMember m WHERE m.invitedBy.id = :userId")
    void deleteByInvitedById(@Param("userId") Long userId);

    @Query("SELECT m FROM ProjectMember m JOIN FETCH m.project p JOIN FETCH p.createdBy WHERE p.key = :projectKey")
    List<ProjectMember> findAllByProjectKeyWithDetails(@Param("projectKey") String projectKey);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProjectMember m WHERE m.invitedBy = :inviter AND m.project = :project AND m.member = :member")
    void deleteByInviterAndProjectAndMember(
            @Param("inviter") User inviter,
            @Param("project") Project project,
            @Param("member") User member
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProjectMember m SET m.invitedBy = :newInviter WHERE m.invitedBy = :oldInviter AND m.project = :project AND m.member <> :excludeMember")
    void reassignInviterForProject(
            @Param("oldInviter") User oldInviter,
            @Param("newInviter") User newInviter,
            @Param("project") Project project,
            @Param("excludeMember") User excludeMember
    );
}
