package com.milvus.vector_spring.project;

import com.milvus.vector_spring.config.jwt.CustomUserDetails;
import com.milvus.vector_spring.project.dto.ProjectCreateRequestDto;
import com.milvus.vector_spring.project.dto.ProjectResponseDto;
import com.milvus.vector_spring.project.dto.ProjectUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("project")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectResponseDto> findAllProjects() {
        return projectService.findAllProject().stream()
                .map(ProjectResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto findOneProject(@PathVariable Long id) {
        return ProjectResponseDto.from(projectService.findOneProject(id));
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto findOneProjectByKey(@RequestParam String key) {
        return ProjectResponseDto.from(projectService.findOneProjectByKey(key));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto createProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody ProjectCreateRequestDto request) {
        return ProjectResponseDto.from(projectService.create(user.getId(), request.name(), request.dimensions()));
    }

    @PatchMapping("/{key}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto updateProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String key, @Validated @RequestBody ProjectUpdateRequestDto request) {
        return ProjectResponseDto.from(projectService.updateProject(user.getId(), key, request));
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String key) {
        projectService.delete(user.getId(), key);
    }
}
