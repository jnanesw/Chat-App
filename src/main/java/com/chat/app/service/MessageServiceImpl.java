package com.chat.app.service;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.Conversation;
import com.chat.app.model.Message;
import com.chat.app.model.User;
import com.chat.app.payload.MessageRequest;
import com.chat.app.payload.MessageResponse;
import com.chat.app.repository.ChatRepo;
import com.chat.app.repository.ConversationRepository;
import com.chat.app.repository.MessageRepository;
import com.chat.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ChatRepo chatRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ConversationRepository conversationRepo;

    @Override
    public MessageResponse saveMessage(MessageRequest messageRequest) {
        User user = userRepo.findById(messageRequest.getSenderId()).orElseThrow(()->
                    new RuntimeException("No User found in DB with id: " + messageRequest.getSenderId())
                );
        Conversation conversation = conversationRepo.findById(messageRequest.getConversationId()).orElseThrow(()->
                    new RuntimeException("No Conversation found in DB with id: " + messageRequest.getConversationId())
                );

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(messageRequest.getSenderId());
        message.setContent(messageRequest.getContent());
        message.setCreatedAt(System.currentTimeMillis());

        Message savedMessage = messageRepo.save(message);

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessageId(savedMessage.getId());
        messageResponse.setConversationId(savedMessage.getConversation().getId());
        messageResponse.setSenderId(savedMessage.getSenderId());
        messageResponse.setSenderName(user.getUserName());
        messageResponse.setContent(savedMessage.getContent());
        messageResponse.setCreatedAt(savedMessage.getCreatedAt());

        return messageResponse;
    }

//    @Override
//    public List<ChatMessage> getChatHistory() {
//        return new ArrayList<>(chatRepo.findAll());
//    }

    @Override
    public List<Message> getMessages(Long conversationid) {
        Conversation conversation = conversationRepo.findById(conversationid).orElseThrow(()->
                    new RuntimeException("No conversation is found in DB with id: " + conversationid)
                );

        return conversation.getMessages();
    }
}
