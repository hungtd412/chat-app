package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "JOIN Participant p ON c.id = p.conversationId " +
           "LEFT JOIN Message m ON c.id = m.conversation.id " +
           "WHERE p.userId = :userId " +
           "GROUP BY c.id " +
           "ORDER BY COALESCE(MAX(m.id), 0) DESC")
    List<Conversation> findAllByUserIdOrderByNewestMessage(@Param("userId") Long userId);
}
