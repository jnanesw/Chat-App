package com.chat.app.service;

import com.chat.app.model.Conversation;
import com.chat.app.model.ConversationParticipant;
import com.chat.app.model.Message;
import com.chat.app.model.User;
import com.chat.app.payload.ConversationRequest;
import com.chat.app.payload.ConversationResponse;
import com.chat.app.payload.GroupCreateRequest;
import com.chat.app.payload.GroupCreateResponse;
import com.chat.app.repository.ConversationRepository;
import com.chat.app.repository.ParticipantRepository;
import com.chat.app.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService{

    @Autowired
    private ConversationRepository conversationRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ParticipantRepository participantRepo;

    @Override
    public List<ConversationResponse> getAllConversations(Long userid) {
        User user = userRepo.findById(userid).orElseThrow(()-> new RuntimeException("User not found in DB!!"));

        List<Conversation> conversations = new ArrayList<>();
        List<ConversationParticipant> conversationParticipants = user.getParticipants();
        if (conversationParticipants.isEmpty()){
            throw new RuntimeException("No conversations found for the user.");
        }

        for(ConversationParticipant cp: conversationParticipants){
            conversations.add(cp.getConversation());
        }

        List<ConversationResponse> conversationResponses = conversations.stream().map((conversation -> {
            ConversationResponse conversationResponse = new ConversationResponse();
            conversationResponse.setId(conversation.getId());
            conversationResponse.setConversationType(conversation.getConversationType());
            conversationResponse.setCreatedAt(conversation.getCreatedAt());
            conversationResponse.setMessages(conversation.getMessages());

            return conversationResponse;
        })).toList();

        return conversationResponses;
    }

//    @Override
//    public List<Message> getMessages(Long conversationid){
//        List<Message> messages = conversationRepo.findById(conversationid).get().getMessages();
//
//        if(messages.isEmpty()) throw new RuntimeException("Conversation with the " + conversationid + " is not present in DB!!");
//
//        return messages;
//    }

    @Override
    public ConversationResponse createConversation(ConversationRequest conversationRequest) {
        User user = userRepo.findByUserName(conversationRequest.getOtherUserName());
        if(user == null){
            throw new RuntimeException("User not found in DB!!");
        }

        Conversation conversation = new Conversation();
        conversation.setConversationType(conversationRequest.getConversationType());
        conversation.setCreatedAt(System.currentTimeMillis());

        Conversation savedConversation = conversationRepo.save(conversation);

        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setId(savedConversation.getId());
        conversationResponse.setConversationType(savedConversation.getConversationType());
        conversationResponse.setCreatedAt(savedConversation.getCreatedAt());

        User currentUser = userRepo.findById(conversationRequest.getCurrentUserId()).orElseThrow(()->
                new RuntimeException("Invalid current User Id")
        );
        // Add User A
        ConversationParticipant participantA = new ConversationParticipant();
        participantA.setConversation(savedConversation);
        participantA.setUser(currentUser);
        participantA.setJoinedAt(System.currentTimeMillis());
        participantRepo.save(participantA);

        // Add User B
        ConversationParticipant participantB = new ConversationParticipant();
        participantB.setConversation(savedConversation);
        participantB.setUser(userRepo.findByUserName(conversationRequest.getOtherUserName()));
        participantB.setJoinedAt(System.currentTimeMillis());
        participantRepo.save(participantB);

        return conversationResponse;
    }

    @Override
    public GroupCreateResponse createGroup(GroupCreateRequest groupCreateRequest) {
        Conversation conversation = new Conversation();
        conversation.setConversationType("GROUP");
        conversation.setCreatedAt(System.currentTimeMillis());

        Conversation savedConversation = conversationRepo.save(conversation);

        Long joinedAt = System.currentTimeMillis();
        List<ConversationParticipant> participantList = groupCreateRequest.getParticipants().stream().map((participant) -> {
            User user = userRepo.findById(participant).orElseThrow(()->
                    new RuntimeException("User not found in DB with id: "+ participant));
            ConversationParticipant newParticipant = new ConversationParticipant();
            newParticipant.setConversation(conversation);
            newParticipant.setUser(user);
            newParticipant.setJoinedAt(joinedAt);

            return newParticipant;
        }).toList();
        participantRepo.saveAll(participantList);

        GroupCreateResponse groupCreateResponse = new GroupCreateResponse();
        groupCreateResponse.setConversationId(conversation.getId());
        groupCreateResponse.setParticipants(groupCreateRequest.getParticipants());
        groupCreateResponse.setConversationType(savedConversation.getConversationType());
        groupCreateResponse.setCreatedAt(System.currentTimeMillis());

        return groupCreateResponse;
    }

    @Override
    public ConversationResponse addParticipant(String userName, Long conversationid) {
        Conversation conversation = conversationRepo.findById(conversationid).orElseThrow(()->
                    new RuntimeException("No conversation is found in DB with id: " + conversationid)
                );

        User user = userRepo.findByUserName(userName);
        ConversationParticipant conversationParticipant = new ConversationParticipant();
        conversationParticipant.setConversation(conversation);
        conversationParticipant.setUser(user);
        conversationParticipant.setJoinedAt(System.currentTimeMillis());

        participantRepo.save(conversationParticipant);

        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setId(conversation.getId());
        conversationResponse.setConversationType(conversation.getConversationType());
        conversationResponse.setCreatedAt(conversation.getCreatedAt());

        return conversationResponse;
    }

}
