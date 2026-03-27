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
    private final InviteService inviteService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponseDto inviteUser(@Validated @RequestBody InviteUserRequestDto inviteUserRequestDto) {
        Invite invite = inviteService.inviteUser(inviteUserRequestDto);
        return new InviteResponseDto(invite);
    }

    @GetMapping("/list/my")
    @ResponseStatus(HttpStatus.OK)
    public List<CombinedProjectListResponseDto> invitedProjectAndCreateProjectList(@RequestParam("userId") Long userId) {
        return inviteService.invitedProjectAndCreateProjectList(userId);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public InvitedProjectUserResponseDto invitedProjectUserList(@RequestParam("key") String projectKey) {
        return inviteService.getInvitedProjectUserList(projectKey);
    }

    @PatchMapping("/change/master")
    @ResponseStatus(HttpStatus.OK)
    public UpdateMasterUserResponseDto changeMasterUser(@Validated @RequestBody UpdateMasterUserRequestDto updateMasterUserRequestDto) {
        return inviteService.updateMasterUser(updateMasterUserRequestDto);
    }

    @DeleteMapping("/banish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public BanishedUserResponseDto banishUser(@Validated @RequestBody BanishUserRequestDto banishUserRequestDto) {
        Invite invite = inviteService.banishUserFromProject(banishUserRequestDto);
        return new BanishedUserResponseDto(invite.getReceivedEmail());

    }
}
