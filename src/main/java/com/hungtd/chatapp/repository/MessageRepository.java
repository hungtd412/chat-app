package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @NativeQuery("SELECT * FROM messages m WHERE m.conversation_id = :conversationId AND m.deleted_at IS NULL ORDER BY m.id DESC LIMIT :limit")
    List<Message> findRecentMessagesByConversationIdWithLimit(@Param("conversationId") Long conversationId, @Param("limit") int limit);
    
    @NativeQuery("SELECT * FROM messages m WHERE m.conversation_id = :conversationId AND m.deleted_at IS NULL ORDER BY m.id DESC")
    List<Message> findAllByConversationIdOrderByIdDesc(@Param("conversationId") Long conversationId);
}
