package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.ChatRequestDto;
import com.milvus.vector_spring.chat.dto.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public ChatResponseDto search(@RequestBody ChatRequestDto chatRequestDto) {
        return chatService.chat(chatRequestDto);
    }
}
