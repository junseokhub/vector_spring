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

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectResponseDto> findAllProjects() {
        List<Project> projects = projectService.findAllProject();
        return projects.stream()
                .map(ProjectResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto findOneProject(@PathVariable Long id) {
        Project project = projectService.findOneProject(id);
        return ProjectResponseDto.from(project);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto findOneProjectByKey(@RequestParam String key) {
        Project project = projectService.findOneProjectByKey(key);
        return ProjectResponseDto.from(project);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDto createProject(@Validated @RequestBody ProjectCreateRequestDto projectCreateRequestDto) {
       return ProjectResponseDto.from(projectService.createProject(projectCreateRequestDto));
    }

    @PostMapping("/update/{key}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectResponseDto updateProject(@PathVariable String key, @Validated @RequestBody ProjectUpdateRequestDto projectUpdateRequestDto) {
        return ProjectResponseDto.from(projectService.updateProject(key, projectUpdateRequestDto));
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String deleteProject(@RequestBody ProjectDeleteRequestDto projectDeleteRequestDto) {
        return projectService.deleteProject(projectDeleteRequestDto);
    }
}
