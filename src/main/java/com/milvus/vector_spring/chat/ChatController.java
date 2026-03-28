package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.ChatRequestDto;
import com.milvus.vector_spring.chat.dto.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatAsyncService chatAsyncService;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<ChatResponseDto> search(@RequestBody ChatRequestDto chatRequestDto) {
        return chatAsyncService.chatAsync(chatRequestDto);
    }
}
