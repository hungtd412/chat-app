package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query(value = "SELECT * FROM messages m WHERE m.conversation_id = :conversationId AND m.deleted_at IS NULL ORDER BY m.id DESC LIMIT :limit", nativeQuery = true)
    List<Message> findRecentMessagesByConversationIdWithLimit(@Param("conversationId") Long conversationId, @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM messages m WHERE m.conversation_id = :conversationId AND m.deleted_at IS NULL ORDER BY m.id DESC", nativeQuery = true)
    List<Message> findAllByConversationIdOrderByIdDesc(@Param("conversationId") Long conversationId);
}
