package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = "SELECT *\n" +
            "FROM messages m\n" +
            "WHERE m.conversation_id = :conversationId\n" +
            "  AND (\n" +
            "    (:offset > 0 AND m.id < :offset)\n" +
            "        OR (:offset <= 0)\n" +
            "    )\n" +
            "ORDER BY m.id DESC\n" +
            "LIMIT :limit", nativeQuery = true)
    List<Message> findAllByConversationIdOrderByIdDesc(
            @Param("conversationId") Long conversationId, @Param("offset") Long offset,
            @Param("limit") Long limit
    );
}
