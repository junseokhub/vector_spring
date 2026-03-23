package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.invite.dto.BanishUserRequestDto;
import com.milvus.vector_spring.invite.dto.InviteUserRequestDto;
import com.milvus.vector_spring.invite.dto.UpdateMasterUserRequestDto;
import com.milvus.vector_spring.invite.dto.UpdateMasterUserResponseDto;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectRepository;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class InviteServiceTest {

    @Autowired private InviteService inviteService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;

    private User masterUser;
    private User receivedUser;
    private Project project;

    @BeforeEach
    void setUp() {
        masterUser = saveUser("master@example.com");
        receivedUser = saveUser("received@example.com");
        project = saveProject("Test Project", "project-key", masterUser);
    }

    @Test
    void invite_success() {
        Invite savedInvite = inviteService.inviteUser(createInviteDto(masterUser, receivedUser, project));
        assertThat(savedInvite).isNotNull();
        assertThat(savedInvite.getReceivedEmail()).isEqualTo(receivedUser.getEmail());
    }

    @Test
    void invite_fail_master_is_not_project_owner() {
        InviteUserRequestDto dto = createInviteDto(receivedUser, receivedUser, project);
        assertThrows(CustomException.class, () -> inviteService.inviteUser(dto));
    }

    @Test
    void banish_success_master_is_not_project_owner() {
        inviteService.inviteUser(createInviteDto(masterUser, receivedUser, project));

        BanishUserRequestDto dto = BanishUserRequestDto.builder()
                .masterUserEmail(masterUser.getEmail())
                .banishedEmail(receivedUser.getEmail())
                .projectKey(project.getKey())
                .build();

        Invite result = inviteService.banishUserFromProject(dto);
        assertThat(result.getCreatedBy().getEmail()).isEqualTo(dto.getBanishedEmail());
    }

    @Test
    void banish_fail_master_is_not_project_owner() {
        BanishUserRequestDto dto = BanishUserRequestDto.builder()
                .masterUserEmail(receivedUser.getEmail())
                .banishedEmail(masterUser.getEmail())
                .projectKey(project.getKey())
                .build();

        assertThrows(CustomException.class, () -> inviteService.banishUserFromProject(dto));
    }

    @Test
    void master_user_update_success() {
        inviteService.inviteUser(createInviteDto(masterUser, receivedUser, project));

        UpdateMasterUserRequestDto dto = UpdateMasterUserRequestDto.builder()
                .createdUserId(masterUser.getId())
                .changeMasterUser(receivedUser.getEmail())
                .projectKey(project.getKey())
                .build();

        UpdateMasterUserResponseDto result = inviteService.updateMasterUser(dto);
        assertThat(result.afterMasterEmail()).isEqualTo(dto.getChangeMasterUser());
    }

    private User saveUser(String email) {
        User user = User.builder()
                .email(email)
                .username("user")
                .password("password")
                .build();
        return userRepository.save(user);
    }

    private Project saveProject(String name, String key, User creator) {
        Project project = Project.builder()
                .name(name)
                .key(key)
                .createdBy(creator)
                .build();
        return projectRepository.save(project);
    }

    private InviteUserRequestDto createInviteDto(User inviteUser, User receiveUser, Project project) {
        return InviteUserRequestDto.builder()
                .inviteId(inviteUser.getId())
                .receiveEmail(receiveUser.getEmail())
                .projectKey(project.getKey())
                .build();
    }
}

