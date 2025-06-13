package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Since max ID is newest, we should order by ID DESC instead of createdAt
    List<Message> findByConversationOrderByIdDesc(Conversation conversation);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.deletedAt IS NULL ORDER BY m.id DESC")
    List<Message> findRecentMessagesByConversationId(@Param("conversationId") Long conversationId);
    
    @Query(value = "SELECT * FROM messages m WHERE m.conversation_id = :conversationId AND m.deleted_at IS NULL ORDER BY m.id DESC LIMIT :limit", nativeQuery = true)
    List<Message> findRecentMessagesByConversationIdWithLimit(@Param("conversationId") Long conversationId, @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM messages m WHERE m.conversation_id = :conversationId AND m.id < :messageId ORDER BY m.id DESC LIMIT :limit", nativeQuery = true)
    List<Message> findMessagesBefore(@Param("conversationId") Long conversationId, 
                                     @Param("messageId") Long messageId, 
                                     @Param("limit") int limit);
}
