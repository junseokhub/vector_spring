package com.milvus.vector_spring.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectUpdateRequestDto {
    private String name;
    private String openAiKey;
    private String prompt;
    private String embedModel;
    private String chatModel;
    private long dimensions;

    @NotNull
    private Long updatedUserId;

    @Builder
    public ProjectUpdateRequestDto(String name, String openAiKey, String prompt, String embedModel, String chatModel, long dimensions, Long updatedUserId) {
        this.name = name;
        this.openAiKey = openAiKey;
        this.prompt = prompt;
        this.embedModel = embedModel;
        this.chatModel = chatModel;
        this.dimensions = dimensions;
        this.updatedUserId = updatedUserId;
    }
}
