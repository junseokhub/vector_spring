package com.milvus.vector_spring.chat;

import com.milvus.vector_spring.chat.dto.ChatRequestDto;
import com.milvus.vector_spring.chat.dto.ChatResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping()
    public ChatResponseDto search(@RequestBody ChatRequestDto chatRequestDto, HttpSession session) {
        if (session.getAttribute("sessionId") == null) {
            String sessionId = UUID.randomUUID().toString();
            session.setAttribute("sessionId", sessionId);
        }
        String sessionId = (String) session.getAttribute("sessionId");
        return chatService.chat(chatRequestDto, sessionId);
    }
}
