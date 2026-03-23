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

    @PostMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CombinedProjectListResponseDto> invitedProjectAndCreateProjectList(@Validated @RequestBody InvitedProjectMyProjectRequestDto invitedProjectMyProjectRequestDto) {
        return inviteService.invitedProjectAndCreateProjectList(invitedProjectMyProjectRequestDto);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitedProjectUserResponseDto invitedProjectUserList(@RequestParam("key") String projectKey) {
        return inviteService.getInvitedProjectUserList(projectKey);
    }

    @PostMapping("/change/master")
    @ResponseStatus(HttpStatus.CREATED)
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
