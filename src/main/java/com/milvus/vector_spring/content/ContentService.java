package com.milvus.vector_spring.content;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.common.service.EncryptionService;
import com.milvus.vector_spring.llm.LlmPlatform;
import com.milvus.vector_spring.llm.dto.EmbedRequestDto;
import com.milvus.vector_spring.llm.dto.EmbedResponseDto;
import com.milvus.vector_spring.llm.provider.LlmProviderRouter;
import com.milvus.vector_spring.milvus.MilvusService;
import com.milvus.vector_spring.milvus.dto.InsertRequestDto;
import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import com.milvus.vector_spring.user.User;
import com.milvus.vector_spring.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserService userService;
    private final ProjectService projectService;
    private final EncryptionService encryptionService;
    private final LlmProviderRouter llmProviderRouter;
    private final MilvusService milvusService;

    public List<Content> findAllContent() {
        return contentRepository.findAll();
    }

    public Content findOneContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_CONTENT));
    }

    public Content findOneContentByContentKey(String contentKey) {
        return contentRepository.findOneContentByKey(contentKey);
    }

    public Content findOneContentByContentId(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.NOT_FOUND_CONTENT));
    }

    public List<Content> findAllContentByProject(String projectKey) {
        return contentRepository.findByProjectKey(projectKey);
    }

    @Transactional
    public Content create(long userId, String projectKey, String title, String answer) {
        User user = userService.findOneUser(userId);
        Project project = projectService.findOneProjectByKey(projectKey);

        LlmPlatform platform = project.getLlmPlatform() != null ? project.getLlmPlatform() : LlmPlatform.OPENAI;

        if (platform != LlmPlatform.OLLAMA
                && (project.getApiKey() == null || project.getApiKey().isEmpty())) {
            throw new CustomException(ErrorStatus.REQUIRE_OPEN_AI_INFO);
        }
        if (project.getEmbedModel() == null || project.getEmbedModel().isEmpty() || project.getDimensions() == 0) {
            throw new CustomException(ErrorStatus.REQUIRE_OPEN_AI_INFO);
        }

        String apiKey = (platform == LlmPlatform.OLLAMA)
                ? null
                : encryptionService.decryptData(project.getApiKey());
        Content content = Content.builder()
                .key(UUID.randomUUID().toString())
                .title(title)
                .answer(answer)
                .project(project)
                .createdBy(user)
                .updatedBy(user)
                .build();

        String embeddingText = content.getTitle() + "\n" + content.getAnswer();
        EmbedResponseDto embedResponse = llmProviderRouter.embed(
                EmbedRequestDto.from(platform, apiKey, project.getEmbedModel(), embeddingText, project.getDimensions())
        );

        if (embedResponse.embedding() == null || embedResponse.embedding().isEmpty()) {
            throw new CustomException(ErrorStatus.MILVUS_DATABASE_ERROR);
        }

        Content savedContent = contentRepository.save(content);
        insertIntoMilvus(savedContent, embedResponse.embedding(), project.getId());

        return savedContent;
    }

    @Transactional
    public Content update(long contentId, long updatedUserId, String title, String answer) {
        User user = userService.findOneUser(updatedUserId);
        Content content = contentRepository.findByIdWithProjectAndUser(contentId);
        if (content == null) throw new CustomException(ErrorStatus.NOT_FOUND_CONTENT);

        Project project = content.getProject();

        if (!content.getAnswer().equals(answer)) {
            LlmPlatform platform = project.getLlmPlatform() != null ? project.getLlmPlatform() : LlmPlatform.OPENAI;
            content.update(title, answer, user);
            String apiKey = (platform == LlmPlatform.OLLAMA)
                    ? null
                    : encryptionService.decryptData(project.getApiKey());
            String embeddingText = content.getTitle() + "\n" + content.getAnswer();
            EmbedResponseDto embedResponse = llmProviderRouter.embed(
                    EmbedRequestDto.from(platform, apiKey, project.getEmbedModel(), embeddingText, project.getDimensions())
            );
            insertIntoMilvus(content, embedResponse.embedding(), project.getId());
        }

        return content;
    }

    private void insertIntoMilvus(Content content, List<Float> vector, Long dbKey) {
        try {
            milvusService.upsertCollection(content.getId(),
                    new InsertRequestDto(content.getId(), vector, content.getTitle(), content.getAnswer()),
                    dbKey);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.MILVUS_DATABASE_ERROR);
        }
    }
}
