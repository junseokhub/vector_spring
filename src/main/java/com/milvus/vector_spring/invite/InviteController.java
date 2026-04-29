package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.invite.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public InviteResponseDto inviteUser(@Validated @RequestBody InviteUserRequestDto request) {
        return InviteResponseDto.from(
                projectMemberService.addMember(request.inviteId(), request.receiveEmail(), request.projectKey())
        );
    }

    @GetMapping("/list/my")
    @ResponseStatus(HttpStatus.OK)
    public List<CombinedProjectListResponseDto> listMyProjects(@RequestParam("userId") Long userId) {
        return projectMemberService.listAllAccessibleProjects(userId);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public InvitedProjectUserResponseDto listProjectMembers(@RequestParam("key") String projectKey) {
        return projectMemberService.getProjectMembers(projectKey);
    }

    @PatchMapping("/change/master")
    @ResponseStatus(HttpStatus.OK)
    public UpdateMasterUserResponseDto transferOwnership(@Validated @RequestBody UpdateMasterUserRequestDto request) {
        return projectMemberService.transferOwnership(
                request.createdUserId(), request.changeMasterUser(), request.projectKey()
        );
    }

    @DeleteMapping("/banish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public BanishedUserResponseDto removeMember(@Validated @RequestBody BanishUserRequestDto request) {
        ProjectMember removed = projectMemberService.removeMember(
                request.masterUserEmail(), request.banishedEmail(), request.projectKey()
        );
        return new BanishedUserResponseDto(removed.getMemberEmail());
    }
}
