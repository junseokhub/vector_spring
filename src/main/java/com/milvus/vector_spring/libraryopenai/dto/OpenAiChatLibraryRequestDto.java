package com.milvus.vector_spring.libraryopenai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OpenAiChatLibraryRequestDto {
    private String openAiKey;
    @NotNull
    private String userMessages;
    private String systemMessages;
    private String model;


}


