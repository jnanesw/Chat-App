package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.Message;
import com.chat.app.payload.MessageRequest;
import com.chat.app.payload.MessageResponse;
import com.chat.app.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    // Message mapping to handle messages from client-side destination (/app/sendMessage)
    @MessageMapping("/chat.sendMessage")
    public MessageResponse sendMessage(MessageRequest messageRequest){
        return messageService.saveMessage(messageRequest);
    }

//    @GetMapping("/api/chatHistory")
//    public List<ChatMessage> getChatHistory(){
//        return chatService.getChatHistory();
//    }

    @GetMapping("/chatHistory/{conversationid}/messages")
    @ResponseBody
    public List<Message> getMessages(@PathVariable Long conversationid){
        return messageService.getMessages(conversationid);
    }

}
