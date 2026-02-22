package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    // Serve the chat HTML at /chat (keep endpoint as /chat per requirement)
    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    // Message mapping to handle messages from client-side destination (/app/sendMessage)
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage chatMessage){
        return chatMessage;
    }
}
