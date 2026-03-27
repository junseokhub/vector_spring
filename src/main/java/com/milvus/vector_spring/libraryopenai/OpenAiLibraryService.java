package com.milvus.vector_spring.libraryopenai;

import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.libraryopenai.dto.OpenAiChatLibraryRequestDto;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.embeddings.CreateEmbeddingResponse;

public interface OpenAiLibraryService {

    ChatCompletion chat(OpenAiChatLibraryRequestDto openAiChatLibraryRequestDto) throws CustomException;

    CreateEmbeddingResponse embedding(String openAiKey, String input, long dimension, String embedModel) throws CustomException;
}
