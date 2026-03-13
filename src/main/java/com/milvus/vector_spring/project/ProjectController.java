package com.milvus.vector_spring.project;

import com.milvus.vector_spring.project.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("project")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping()
    public List<ProjectResponseDto> findAllProjects() {
        List<Project> projects = projectService.findAllProject();
        return projects.stream()
                .map(ProjectResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProjectResponseDto findOneProject(@PathVariable Long id) {
        Project project = projectService.findOneProject(id);
        return ProjectResponseDto.from(project);
    }

    @GetMapping("/search")
    public ProjectResponseDto findOneProjectByKey(@RequestParam String key) {
        Project project = projectService.findOneProjectByKey(key);
        return ProjectResponseDto.from(project);
    }

    @GetMapping("/contents/{key}")
    public ProjectContentsResponseDto findOneProjectWithContents(@PathVariable String key) {
        return projectService.getProjectWithContents(key);
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDto> createProject(@Validated @RequestBody ProjectCreateRequestDto projectCreateRequestDto) {
        Project project = projectService.createProject(projectCreateRequestDto);
        return ResponseEntity.ok(ProjectResponseDto.from(project));
    }

    @PostMapping("/update/{key}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable String key, @Validated @RequestBody ProjectUpdateRequestDto projectUpdateRequestDto) {
        Project project = projectService.updateProject(key, projectUpdateRequestDto);
        return ResponseEntity.ok(ProjectResponseDto.from(project));
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteProject(@RequestBody ProjectDeleteRequestDto projectDeleteRequestDto) {
        String deleteProject = projectService.deleteProject(projectDeleteRequestDto);
        return ResponseEntity.ok(deleteProject);
    }
}
