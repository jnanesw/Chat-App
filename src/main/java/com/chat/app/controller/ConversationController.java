package com.chat.app.controller;

import com.chat.app.payload.ConversationRequest;
import com.chat.app.payload.ConversationResponse;
import com.chat.app.payload.GroupCreateRequest;
import com.chat.app.payload.GroupCreateResponse;
import com.chat.app.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping("/users/{userid}/conversations")
    public List<ConversationResponse> getAllConversations(@PathVariable Long userid){
        return conversationService.getAllConversations(userid);
    }


    @PostMapping("/conversations/newChat")
    public ConversationResponse createConversation(@RequestBody ConversationRequest conversationRequest){
        return conversationService.createConversation(conversationRequest);
    }

    @PostMapping("/conversations/newGroup")
    public GroupCreateResponse createGroup(@RequestBody GroupCreateRequest groupCreateRequest){
        return conversationService.createGroup(groupCreateRequest);
    }

    @PostMapping("/conversations/{conversationid}/addParticipant")
    public ConversationResponse addParticipant(@RequestBody ConversationRequest conversationRequest, @PathVariable Long conversationid){
        return conversationService.addParticipant(conversationRequest, conversationid);
    }
}
