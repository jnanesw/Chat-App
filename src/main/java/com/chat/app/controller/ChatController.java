package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

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

    // REST endpoint so you can test sending a message without the WebSocket front end
    @PostMapping(path = "/chat/send", consumes = "application/json")
    public ResponseEntity<String> sendViaRest(@RequestBody ChatMessage chatMessage) {
        messagingTemplate.convertAndSend("/topic/messages", chatMessage);
        return ResponseEntity.ok("Message sent to /topic/messages");
    }
}
