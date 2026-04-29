package com.milvus.vector_spring.project;

import com.milvus.vector_spring.project.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto createProject(@Validated @RequestBody ProjectCreateRequestDto request) {
        return ProjectResponseDto.from(projectService.create(request.createdUserId(), request.name(), request.dimensions()));
    }

    @PostMapping("/update/{key}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto updateProject(@PathVariable String key, @Validated @RequestBody ProjectUpdateRequestDto request) {
        return ProjectResponseDto.from(projectService.updateProject(key, request));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@RequestBody ProjectDeleteRequestDto request) {
        projectService.delete(request.userId(), request.key());
    }
}
