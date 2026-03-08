package com.chat.app.repository;

import com.chat.app.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    List<ConversationParticipant> findByConversationId(Long conversationId);
}
