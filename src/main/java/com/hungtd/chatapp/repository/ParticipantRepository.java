package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Participant;
import com.hungtd.chatapp.entity.ParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
    
    List<Participant> findByConversationId(Long conversationId);
    
    List<Participant> findAllByConversationId(Long conversationId);
    
    List<Participant> findByUserId(Long userId);
    
    @Query("SELECT p.userId FROM Participant p WHERE p.conversationId = :conversationId")
    List<Long> findUserIdsByConversationId(@Param("conversationId") Long conversationId);
    
    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.conversationId = :conversationId AND p.userId = :userId")
    boolean isUserInConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    void deleteByConversationIdAndUserId(Long conversationId, Long userId);
}
