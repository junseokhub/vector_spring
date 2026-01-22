package com.milvus.vector_spring.project;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.common.service.EncryptionService;
import com.milvus.vector_spring.content.Content;
import com.milvus.vector_spring.content.ContentRepository;
import com.milvus.vector_spring.milvus.MilvusService;
import com.milvus.vector_spring.project.dto.ProjectContentsResponseDto;
import com.milvus.vector_spring.project.dto.ProjectCreateRequestDto;
import com.milvus.vector_spring.project.dto.ProjectDeleteRequestDto;
import com.milvus.vector_spring.project.dto.ProjectUpdateRequestDto;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService  {

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final EncryptionService encryptionService;
    private final ContentRepository contentRepository;
    private final MilvusService milvusService;

    public List<Project> findAllProject() {
        return projectRepository.findAll();
    }

    public Project findOneProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_PROJECT));
    }

    public Project findOneProjectByKey(String key) {
        return projectRepository.findProjectByKey(key)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_PROJECT));
    }

    public ProjectContentsResponseDto findOneProjectWithContents(String key) {
        Project project = projectRepository.findOneProjectWithContents(key)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_PROJECT));
//        List<Content> contentsz = project.getContents();
        List<Content> contents = contentRepository.findByProjectKey(project.getKey());
        return ProjectContentsResponseDto.projectContentsResponseDto(project, contents);
    }

    @Transactional
    public Project createProject(ProjectCreateRequestDto dto) {
        try {
            User user = userService.findOneUser(dto.getCreatedUserId());
            Project project = Project.builder()
                    .name(dto.getName())
                    .key(String.valueOf(UUID.randomUUID()))
                    .dimensions(dto.getDimensions())
                    .totalToken(0)
                    .createdBy(user)
                    .updatedBy(user)
                    .build();
            Project savedProject = projectRepository.save(project);
            milvusService.createSchema(savedProject.getId(), (int) dto.getDimensions());

            Content defaultContent = Content.builder()
                    .key(UUID.randomUUID().toString())
                    .title("[Default] 웰컴 메세지")
                    .answer("안녕하세요.")
                    .project(savedProject)
                    .createdBy(user)
                    .updatedBy(user)
                    .build();
            contentRepository.save(defaultContent);
            return savedProject;
        } catch (Exception e) {
            log.error("Create Project Error: {}", e.getMessage());
            throw new CustomException(ErrorStatus.MILVUS_DATABASE_ERROR);
        }
    }

    @Transactional
    public Project updateProject(String key, ProjectUpdateRequestDto dto) {
        User user = userService.findOneUser(dto.getUpdatedUserId());
        Project project = projectRepository.findProjectByKey(key)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_PROJECT));
        String secretKey = resolveOpenAiKey(project, dto);
        project.update(dto, secretKey, user);
        return project;
    }

    @Transactional
    public String deleteProject(ProjectDeleteRequestDto dto) {
        Project project = projectRepository.findProjectByKey(dto.getKey())
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_PROJECT));
        if (!Objects.equals(project.getCreatedBy().getId(), dto.getUserId())) {
            throw new CustomException(ErrorStatus.NOT_PROJECT_MASTER_USER);
        }
        milvusService.deleteCollection(project.getId());
        projectRepository.delete(project);
        return "Deleted Success!";
    }

    @Transactional
    public void plusTotalToken(String key, long totalToken) {
        Project project = projectRepository.findProjectByKeyForUpdate(key);
        project.updateTotalToken(project.getTotalToken() + totalToken);
    }

    public void updateProjectMaster(Project project, User user) {
        project.updateByUser(user, user);
        projectRepository.save(project);
    }

    private String resolveOpenAiKey(Project project, ProjectUpdateRequestDto dto) {
        String projectKey = project.getOpenAiKey();
        String dtoKey = dto.getOpenAiKey();

        boolean isProjectKeyEmpty = projectKey == null || projectKey.isEmpty();
        boolean isDtoKeyEmpty = dtoKey == null || dtoKey.isEmpty();

        if (isProjectKeyEmpty && isDtoKeyEmpty) {
            return null;
        }
        if (isProjectKeyEmpty) {
            return encryptionService.encryptData(dtoKey);
        }
        if (!isDtoKeyEmpty) {
            String decryptedProjectKey = encryptionService.decryptData(projectKey);
            if (decryptedProjectKey.equals(dtoKey)) {
                return projectKey;
            }
            return encryptionService.encryptData(dtoKey);
        }
        return projectKey;
    }

    public String decryptOpenAiKey(Project project) {
        if (project.getOpenAiKey() == null || project.getOpenAiKey().isEmpty()) {
            throw new CustomException(ErrorStatus.REQUIRE_OPEN_AI_INFO);
        }
        return encryptionService.decryptData(project.getOpenAiKey());
    }
}
