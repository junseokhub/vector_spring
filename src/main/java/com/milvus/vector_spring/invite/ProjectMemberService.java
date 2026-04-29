package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.invite.dto.CombinedProjectListResponseDto;
import com.milvus.vector_spring.invite.dto.InvitedProjectUserResponseDto;
import com.milvus.vector_spring.invite.dto.UpdateMasterUserResponseDto;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public List<CombinedProjectListResponseDto> listAllAccessibleProjects(Long userId) {
        userService.findOneUser(userId);
        List<CombinedProjectListResponseDto> owned = projectService.findMyProjectsAsDto(userId);
        List<CombinedProjectListResponseDto> joined = projectMemberRepository.findInvitedProjectsAsDto(userId);

        return Stream.concat(owned.stream(), joined.stream())
                .sorted(Comparator.comparing(CombinedProjectListResponseDto::getCreatedAt).reversed())
                .toList();
    }

    @Transactional
    public ProjectMember addMember(Long inviterId, String memberEmail, String projectKey) {
        User inviter = userService.findOneUser(inviterId);
        User newMember = userService.findOneUserByEmail(memberEmail);
        Project project = projectService.findOneProjectByKey(projectKey);

        validateIsOwner(inviter, project);

        return projectMemberRepository.save(ProjectMember.builder()
                .member(newMember)
                .memberEmail(newMember.getEmail())
                .invitedBy(inviter)
                .project(project)
                .role(MemberRole.MEMBER)
                .build());
    }

    @Transactional
    public ProjectMember removeMember(String ownerEmail, String memberEmail, String projectKey) {
        User owner = userService.findOneUserByEmail(ownerEmail);
        User memberToRemove = userService.findOneUserByEmail(memberEmail);
        Project project = projectService.findOneProjectByKey(projectKey);

        validateIsOwner(owner, project);

        ProjectMember membership = projectMemberRepository
                .findByProjectAndMember(project, memberToRemove)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_INVITED_USER));

        projectMemberRepository.delete(membership);
        return membership;
    }

    @Transactional
    public UpdateMasterUserResponseDto transferOwnership(Long currentOwnerId, String newOwnerEmail, String projectKey) {
        User currentOwner = userService.findOneUser(currentOwnerId);
        User newOwner = userService.findOneUserByEmail(newOwnerEmail);
        Project project = projectService.findOneProjectByKey(projectKey);

        projectService.transferOwnership(project, newOwner);

        projectMemberRepository.deleteByInviterAndProjectAndMember(currentOwner, project, newOwner);
        projectMemberRepository.reassignInviterForProject(currentOwner, newOwner, project, newOwner);

        return new UpdateMasterUserResponseDto(project.getKey(), currentOwner.getEmail(), newOwner.getEmail());
    }

    @Transactional(readOnly = true)
    public InvitedProjectUserResponseDto getProjectMembers(String projectKey) {
        List<ProjectMember> members = projectMemberRepository.findAllByProjectKeyWithDetails(projectKey);
        if (members.isEmpty()) {
            return null;
        }
        return InvitedProjectUserResponseDto.from(projectKey, members);
    }

    private void validateIsOwner(User user, Project project) {
        if (!Objects.equals(user.getId(), project.getCreatedBy().getId())) {
            throw new CustomException(ErrorStatus.NOT_PROJECT_MASTER_USER);
        }
    }
}
