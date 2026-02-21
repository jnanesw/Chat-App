package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    // Serve the chat HTML at /chatPage to avoid collision with the SockJS endpoint registered at /chat
    @GetMapping("/chatPage")
    public String chatPage() {
        return "chat";
    }

    // Adjusted message mapping to match the client-side destination (/app/sendMessage)
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage chatMessage){
        return chatMessage;
    }
}
