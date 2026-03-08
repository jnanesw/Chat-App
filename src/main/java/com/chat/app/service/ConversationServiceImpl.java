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
		List<ConversationResponse> conversationResponses = new ArrayList<>();
		
        List<ConversationParticipant> conversationParticipants = user.getParticipants();
        if (conversationParticipants.isEmpty()){
            System.out.println("No conversations found for the user.");
			return conversationResponses;
        }

        for(ConversationParticipant cp: conversationParticipants){
            conversations.add(cp.getConversation());
        }

        conversationResponses = conversations.stream().map((conversation -> {
            ConversationResponse conversationResponse = new ConversationResponse();
            conversationResponse.setId(conversation.getId());
            conversationResponse.setConversationType(conversation.getConversationType());

            if(conversation.getConversationType().equalsIgnoreCase("ONE_TO_ONE")){
                System.out.println("Entered inside ONE_TO_ONE");
                List<ConversationParticipant> participants = participantRepo.findByConversationId(conversation.getId());

                User receiverUser = participants.stream()
                        .map(ConversationParticipant::getUser)
                        .filter( user1 -> !user1.getId().equals(userid))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No other participant found"));

                conversationResponse.setReceiverName(receiverUser.getUserName());
            }
            System.out.println("Exited inside ONE_TO_ONE: " + conversationResponse.getReceiverName());
            if(conversation.getConversationType().equalsIgnoreCase("GROUP")){
                System.out.println("Entered inside GROUP: " + conversation.getGroupName());
                conversationResponse.setReceiverName(conversation.getGroupName());
            }
            System.out.println("Exited inside GROUP: " + conversationResponse.getReceiverName());

            conversationResponse.setCreatedAt(conversation.getCreatedAt());
            conversationResponse.setMessages(conversation.getMessages());

            return conversationResponse;
        })).toList();

        return conversationResponses;
    }

    @Override
    public ConversationResponse createConversation(ConversationRequest conversationRequest) {
        User user = userRepo.findByUserName(conversationRequest.getOtherUserName());
		System.out.println("Newly added UserName: " + conversationRequest.getOtherUserName());
		System.out.println("Checked in DB: "+ user);
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
        conversationResponse.setReceiverName(conversationRequest.getOtherUserName());
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
        userRepo.findById(groupCreateRequest.getCurrentUserId()).orElseThrow(()->
                    new RuntimeException("User with id: " + groupCreateRequest.getCurrentUserId() +
                            " not found in DB")
                );

        GroupCreateResponse groupCreateResponse = new GroupCreateResponse();
        Long joinedAt = System.currentTimeMillis();

        if(groupCreateRequest.getConversationID() == null){
            Conversation conversation = new Conversation();

            conversation.setConversationType("GROUP");
            conversation.setGroupName(groupCreateRequest.getGroupName());
            conversation.setCreatedAt(System.currentTimeMillis());

            Conversation savedConversation = conversationRepo.save(conversation);

            List<ConversationParticipant> participantList = groupCreateRequest.getParticipants().stream().map((participant) -> {
                User user = userRepo.findByUserName(participant);
                if(user == null) throw new RuntimeException("User not found in DB with id: "+ participant);

                ConversationParticipant newParticipant = new ConversationParticipant();
                newParticipant.setConversation(savedConversation);
                newParticipant.setUser(user);
                newParticipant.setJoinedAt(joinedAt);

                return newParticipant;
            }).toList();
            participantRepo.saveAll(participantList);

            groupCreateResponse.setConversationId(conversation.getId());
            groupCreateResponse.setGroupName((conversation.getGroupName()));
            groupCreateResponse.setParticipants(groupCreateRequest.getParticipants());
            groupCreateResponse.setConversationType(savedConversation.getConversationType());
            groupCreateResponse.setCreatedAt(joinedAt);

        }
        if(groupCreateRequest.getConversationID() != null){
            User user = userRepo.findByUserName(groupCreateRequest.getParticipants().get(0));
            if(user == null){
                throw new RuntimeException("No user found with username: " + groupCreateRequest.getParticipants().get(0));
            }

            Conversation conversation = conversationRepo.findById(groupCreateRequest.getConversationID())
                    .orElseThrow(() -> new RuntimeException("No conversation with: " + groupCreateRequest.getConversationID() + " is found!"));

            ConversationParticipant newParticipant = new ConversationParticipant();
            newParticipant.setConversation(conversation);
            newParticipant.setUser(user);
            newParticipant.setJoinedAt(joinedAt);
            participantRepo.save(newParticipant);

            groupCreateResponse.setConversationId(conversation.getId());
            groupCreateResponse.setGroupName((conversation.getGroupName()));
            groupCreateResponse.setParticipants(groupCreateRequest.getParticipants());
            groupCreateResponse.setConversationType(conversation.getConversationType());
            groupCreateResponse.setCreatedAt(joinedAt);
        }

        return groupCreateResponse;

    }

    @Override
    public ConversationResponse addParticipant(ConversationRequest conversationRequest, Long conversationid) {
        Conversation conversation = conversationRepo.findById(conversationid).orElseThrow(()->
                    new RuntimeException("No conversation is found in DB with id: " + conversationid)
                );

        User user = userRepo.findByUserName(conversationRequest.getOtherUserName());

        if(user == null){
            throw new RuntimeException("No user found in DB with name: " + conversationRequest.getOtherUserName());
        }

        conversation.getParticipants().stream().map((participant) -> {
            if(participant.getId().equals(user.getId())){
                throw new RuntimeException("User with id: "+ user.getId() + " Already exists in the conversation");
            }

            return participant;
        });

        ConversationParticipant conversationParticipant = new ConversationParticipant();
        conversationParticipant.setConversation(conversation);
        conversationParticipant.setUser(user);
        conversationParticipant.setJoinedAt(System.currentTimeMillis());

        ConversationParticipant savedParticipant = participantRepo.save(conversationParticipant);

        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setId(savedParticipant.getConversation().getId());
        conversationResponse.setConversationType(savedParticipant.getConversation().getConversationType());
        conversationResponse.setCreatedAt(savedParticipant.getConversation().getCreatedAt());

        return conversationResponse;
    }

}
