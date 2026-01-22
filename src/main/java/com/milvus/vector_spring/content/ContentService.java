package com.milvus.vector_spring.content;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.common.service.EncryptionService;
import com.milvus.vector_spring.content.dto.ContentCreateRequestDto;
import com.milvus.vector_spring.content.dto.ContentUpdateRequestDto;
import com.milvus.vector_spring.libraryopenai.OpenAiLibraryService;
import com.milvus.vector_spring.milvus.MilvusService;
import com.milvus.vector_spring.milvus.dto.InsertRequestDto;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserService userService;
    private final ProjectService projectService;
    private final EncryptionService encryptionService;
    private final OpenAiLibraryService openAiLibraryService;
    private final MilvusService milvusService;

    public List<Content> findAllContent() {
        return contentRepository.findAll();
    }

    public Content findOneContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_CONTENT));
    }

    public Optional<Content> findOneContentByContnetKey(String contentKey) {
        return contentRepository.findOneContentByKey(contentKey);
    }

    public Optional<Content> findOneContentByContentId(Long id) {
        return contentRepository.findById(id);
    }

    @Transactional
    public Content createContent(long userId, ContentCreateRequestDto dto) {
        User user = userService.findOneUser(userId);
        Project project = projectService.findOneProjectByKey(dto.getProjectKey());

        if (project.getOpenAiKey().isEmpty()) {
            throw new CustomException(ErrorStatus.REQUIRE_OPEN_AI_INFO);
        }
        if (project.getEmbedModel().isEmpty() || project.getDimensions() == 0) {
            throw new CustomException(ErrorStatus.REQUIRE_OPEN_AI_INFO);
        }

        String key = encryptionService.decryptData(project.getOpenAiKey());
        Content content = Content.builder()
                .key(UUID.randomUUID().toString())
                .title(dto.getTitle())
                .answer(dto.getAnswer())
                .project(project)
                .createdBy(user)
                .updatedBy(user)
                .build();

        CreateEmbeddingResponse embedResponseDto = openAiLibraryService.embedding(key, content.getAnswer(), project.getDimensions());
        if (embedResponseDto == null || embedResponseDto.data().isEmpty()) {
            throw new CustomException(ErrorStatus.MILVUS_DATABASE_ERROR);
        }
        Content savedContent = contentRepository.save(content);
        insertIntoMilvus(savedContent, embedResponseDto, project.getId());

        return savedContent;
    }

    @Transactional
    public Content updateContent(long id, ContentUpdateRequestDto dto) {
        User user = userService.findOneUser(dto.getUpdatedUserId());
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_CONTENT));
        Project project = projectService.findOneProject(content.getProject().getId());

        content.update(dto.getTitle(), dto.getAnswer(), user);

        if (!content.getAnswer().equals(dto.getAnswer())) {
            String key = encryptionService.decryptData(project.getOpenAiKey());
            CreateEmbeddingResponse embedResponseDto = openAiLibraryService.embedding(key, content.getAnswer(), project.getDimensions());
            insertIntoMilvus(content, embedResponseDto, project.getId());
        }

        return content;
    }

    private void insertIntoMilvus(Content content, CreateEmbeddingResponse embedResponseDto, Long dbKey) {
        List<Float> floatList = embedResponseDto.data().get(0).embedding();
        try {
            milvusService.upsertCollection(content.getId(),
                    InsertRequestDto.builder()
                            .id(content.getId())
                            .vector(floatList)
                            .title(content.getTitle())
                            .answer(content.getAnswer())
                            .build(),
                    dbKey);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_DATABASE_ERROR);
        }
    }
}

