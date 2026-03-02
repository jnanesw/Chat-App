package com.chat.app.service;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.Message;
import com.chat.app.payload.MessageRequest;
import com.chat.app.payload.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MessageService {
    public MessageResponse saveMessage(MessageRequest messageRequest);
//    public List<ChatMessage> getChatHistory();

    public List<Message> getMessages(Long conversationid);
}
