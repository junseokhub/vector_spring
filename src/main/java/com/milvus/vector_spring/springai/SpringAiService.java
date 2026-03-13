package com.milvus.vector_spring.springai;

import com.milvus.vector_spring.project.Project;
import com.milvus.vector_spring.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.*;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpringAiService {

    private final SpringAiConnector springAiConnector;
    private final ProjectService projectService;

    public ChatResponse chat(String projectKey, String text) {

        Project project = getProject(projectKey);
        String apiKey = getApiKey(project);

        OpenAiChatModel chatModel =
                springAiConnector.getChatModel(apiKey, project.getChatModel());

        return chatModel.call(new Prompt(new UserMessage(text)));
    }

    public EmbeddingResponse embedding(String projectKey, String text) {

        Project project = getProject(projectKey);
        String apiKey = getApiKey(project);
        Integer dimension = Math.toIntExact(project.getDimensions());

        OpenAiEmbeddingModel embeddingModel =
                springAiConnector.getEmbeddingModel(apiKey, project.getEmbedModel(), dimension);

        EmbeddingOptions options = EmbeddingOptionsBuilder.builder()
                .withModel(project.getEmbedModel())
                .withDimensions(dimension)
                .build();

        EmbeddingRequest request = new EmbeddingRequest(List.of(text), options);

        return embeddingModel.call(request);
    }

    private Project getProject(String projectKey) {
        return projectService.findOneProjectByKey(projectKey);
    }

    private String getApiKey(Project project) {
        return projectService.decryptOpenAiKey(project);
    }
}