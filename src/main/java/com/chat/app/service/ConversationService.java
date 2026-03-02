package com.chat.app.service;

import com.chat.app.model.Message;
import com.chat.app.payload.ConversationRequest;
import com.chat.app.payload.ConversationResponse;
import com.chat.app.payload.GroupCreateRequest;
import com.chat.app.payload.GroupCreateResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationService {
    public List<ConversationResponse> getAllConversations(Long userid);
//    public List<Message> getMessages(Long conversationid);

    public ConversationResponse createConversation(ConversationRequest conversationRequest);

    public ConversationResponse addParticipant(String userName, Long conversationid);

    public GroupCreateResponse createGroup(GroupCreateRequest groupCreateRequest);
}
