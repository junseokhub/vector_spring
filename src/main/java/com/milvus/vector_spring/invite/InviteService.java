package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.invite.dto.*;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public List<CombinedProjectListResponseDto> invitedProjectAndCreateProjectList(Long userId) {
        User user = userService.findOneUser(userId);

        List<CombinedProjectListResponseDto> myProjects = projectService.findMyProjectsAsDto(user.getId());

        List<CombinedProjectListResponseDto> invitedProjects = inviteRepository.findInvitedProjectsAsDto(user.getEmail());

        return Stream.concat(myProjects.stream(), invitedProjects.stream())
                .sorted(Comparator.comparing(CombinedProjectListResponseDto::getCreatedAt).reversed())
                .toList();
    }

    @Transactional
    public Invite inviteUser(InviteUserRequestDto dto) {
        User invitedUser = userService.findOneUser(dto.getInviteId());
        User receivedUser = userService.findOneUserByEmail(dto.getReceiveEmail());
        Project project = projectService.findOneProjectByKey(dto.getProjectKey());

        validateProjectMaster(invitedUser, project);

        Invite invite = Invite.builder()
                .receivedEmail(receivedUser.getEmail())
                .createdBy(invitedUser)
                .project(project)
                .build();

        return inviteRepository.save(invite);
    }

    @Transactional
    public Invite banishUserFromProject(BanishUserRequestDto dto) {
        User masterUser = userService.findOneUserByEmail(dto.getMasterUserEmail());
        User banishUser = userService.findOneUserByEmail(dto.getBanishedEmail());
        Project project = projectService.findOneProjectByKey(dto.getProjectKey());

        validateProjectMaster(masterUser, project);

        Invite invite = inviteRepository.findByProjectAndReceivedEmail(project, banishUser.getEmail())
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_INVITED_USER));

        inviteRepository.delete(invite);
        return invite;
    }

    @Transactional
    public UpdateMasterUserResponseDto updateMasterUser(UpdateMasterUserRequestDto dto) {
        User beforeMaster = userService.findOneUser(dto.getCreatedUserId());
        User afterMaster = userService.findOneUserByEmail(dto.getChangeMasterUser());
        Project project = projectService.findOneProjectByKey(dto.getProjectKey());

        projectService.updateProjectMaster(project, afterMaster);

        List<Invite> invites = inviteRepository.findAllByCreatedByAndProject(beforeMaster, project);

        invites.stream()
                .filter(invite -> invite.getReceivedEmail().equals(afterMaster.getEmail()))
                .findFirst()
                .ifPresent(inviteRepository::delete);

        invites.stream()
                .filter(invite -> !invite.getReceivedEmail().equals(afterMaster.getEmail()))
                .forEach(invite -> invite.updateCreatedBy(afterMaster));
        return new UpdateMasterUserResponseDto(
                project.getKey(),
                beforeMaster.getEmail(),
                afterMaster.getEmail()
        );

    }

    @Transactional(readOnly = true)
    public InvitedProjectUserResponseDto getInvitedProjectUserList(String projectKey) {
        List<Invite> invitedList = inviteRepository.findAllByProjectKeyWithDetails(projectKey);

        if (invitedList.isEmpty()) {
            return null;
        }

        return InvitedProjectUserResponseDto.from(projectKey, invitedList);
    }

    private void validateProjectMaster(User user, Project project) {
        if (!Objects.equals(user.getId(), project.getCreatedBy().getId())) {
            throw new CustomException(ErrorStatus.NOT_PROJECT_MASTER_USER);
        }
    }
}
