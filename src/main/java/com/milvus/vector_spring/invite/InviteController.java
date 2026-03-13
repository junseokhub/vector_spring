package com.milvus.vector_spring.invite;

import com.milvus.vector_spring.invite.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;

    @PostMapping()
    public ResponseEntity<InviteResponseDto> inviteUser(@Validated @RequestBody InviteUserRequestDto inviteUserRequestDto) {
        Invite invite = inviteService.inviteUser(inviteUserRequestDto);
        return ResponseEntity.ok(new InviteResponseDto(invite));
    }

    @PostMapping("/list")
    public List<CombinedProjectListResponseDto> invitedProjectAndCreateProjectList(@Validated @RequestBody InvitedProjectMyProjectRequestDto invitedProjectMyProjectRequestDto) {
        return inviteService.invitedProjectAndCreateProjectList(invitedProjectMyProjectRequestDto);
    }

    @GetMapping("/list")
    public InvitedProjectUserResponseDto invitedProjectUserList(@RequestParam("key") String projectKey) {
        return inviteService.getInvitedProjectUserList(projectKey);
    }

    @PostMapping("/change/master")
    public String changeMasterUser(@Validated @RequestBody UpdateMasterUserRequestDto updateMasterUserRequestDto) {
        inviteService.updateMasterUser(updateMasterUserRequestDto);
        return "Finish change to " + updateMasterUserRequestDto.getChangeMasterUser() + "!!";
    }

    @DeleteMapping("/banish")
    public String banishUser(@Validated @RequestBody BanishUserRequestDto banishUserRequestDto) {
        return inviteService.banishUserFromProject(banishUserRequestDto);
    }
}
