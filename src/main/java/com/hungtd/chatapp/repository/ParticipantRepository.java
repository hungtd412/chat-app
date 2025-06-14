package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Participant;
import com.hungtd.chatapp.entity.ParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
    
    @Query("SELECT p.userId FROM Participant p WHERE p.conversationId = :conversationId")
    List<Long> findUserIdsByConversationId(@Param("conversationId") Long conversationId);
    
    @Query("SELECT p FROM Participant p WHERE p.conversationId = :conversationId AND p.userId = :userId")
    Participant findByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    @Query("SELECT p FROM Participant p WHERE p.conversationId = :conversationId AND p.userId != :userId")
    List<Participant> findOtherParticipants(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    @Query("SELECT p FROM Participant p WHERE p.conversationId = :conversationId")
    List<Participant> findByConversationId(@Param("conversationId") Long conversationId);
    
    // The existsById method is now correctly inherited from JpaRepository<Participant, ParticipantId>
}
