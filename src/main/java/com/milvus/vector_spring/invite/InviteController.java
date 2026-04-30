package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.config.jwt.CustomUserDetails;
import com.milvus.vector_spring.invite.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponseDto inviteUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody InviteUserRequestDto request
    ) {
        return InviteResponseDto.from(
                projectMemberService.addMember(user.getId(), request.receiveEmail(), request.projectKey())
        );
    }

    @GetMapping("/list/my")
    @ResponseStatus(HttpStatus.OK)
    public List<CombinedProjectListResponseDto> listMyProjects(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return projectMemberService.listAllAccessibleProjects(user.getId());
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public InvitedProjectUserResponseDto listProjectMembers(
            @RequestParam String projectKey
    ) {
        return projectMemberService.getProjectMembers(projectKey);
    }

    @PatchMapping("/change/master")
    @ResponseStatus(HttpStatus.OK)
    public UpdateMasterUserResponseDto transferOwnership(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody UpdateMasterUserRequestDto request
    ) {
        return projectMemberService.transferOwnership(
                user.getId(), request.changeMasterUser(), request.projectKey()
        );
    }

    @DeleteMapping("/banish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public BanishedUserResponseDto removeMember(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody BanishUserRequestDto request
    ) {
        ProjectMember removed = projectMemberService.removeMember(
                user.getEmail(), request.banishedEmail(), request.projectKey()
        );
        return new BanishedUserResponseDto(removed.getMemberEmail());
    }
}
