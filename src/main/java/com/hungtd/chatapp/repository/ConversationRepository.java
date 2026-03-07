package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.projection.ConversationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query(value = """
        WITH msgs AS (
            SELECT m.id,
                   m.conversation_id,
                   m.message,
                   m.created_at,
                   ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.id DESC) AS rn
            FROM messages m
        )
        SELECT c.id,
               c.title,
               c.image_url,
               c.type,
               p2.friend_name,
               p2.avt_url,
               m.message AS latest_message,
               m.created_at
        FROM (SELECT id, image_url, title, type FROM conversations) c
        JOIN (SELECT conversation_id, user_id FROM participants) p
             ON p.conversation_id = c.id AND p.user_id = :userId
        LEFT JOIN (
              SELECT user_id, avt_url, conversation_id,
                     CONCAT(first_name, ' ', last_name) AS friend_name
              FROM participants
                   JOIN users ON participants.user_id = users.id
        ) p2
             ON p2.conversation_id = c.id 
            AND p2.user_id <> :userId 
            AND c.type = 'PRIVATE'
        LEFT JOIN msgs m
             ON m.conversation_id = c.id AND m.rn = 1
        ORDER BY COALESCE(m.id, 0) DESC
        """,
            nativeQuery = true)
    List<ConversationProjection> findAllByUserIdOrderByNewestMessage(
            @Param("userId") Long userId
    );

}
