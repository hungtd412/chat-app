package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.Conversation;
import com.hungtd.chatapp.projection.ConversationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @NativeQuery("WITH msgs AS (SELECT m.id,\n" +
            "                     m.conversation_id,\n" +
            "                     m.message,\n" +
            "                     m.created_at,\n" +
            "                     ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.id DESC) AS rn\n" +
            "              FROM messages m)\n" +
            "SELECT c.id,\n" +
            "       c.title,\n" +
            "       c.image_url,\n" +
            "       c.type,\n" +
            "       p2.friend_name,\n" +
            "       p2.avt_url,\n" +
            "       m.message as latest_message,\n" +
            "       m.created_at\n" +

            "FROM (select id, image_url, title, type from conversations) c\n" +
            "         JOIN (select conversation_id, user_id from participants) p\n" +
            "              ON p.conversation_id = c.id AND p.user_id = 2\n" +
            "         LEFT JOIN (select user_id, avt_url, conversation_id,\n" +
            "                      CONCAT(first_name, ' ', last_name) as friend_name\n" +
            "               from participants\n" +
            "                        join users on participants.user_id = users.id) p2\n" +
            "              ON p2.conversation_id = c.id AND p2.user_id <> 2 AND type = 'PRIVATE'\n" +
            "         LEFT JOIN msgs m\n" +
            "                   ON m.conversation_id = c.id AND m.rn = 1 /*first row of each message partition*/\n" +
            "ORDER BY COALESCE(m.id, 0) DESC")
    <T> List<T> findAllByUserIdOrderByNewestMessage(
            @Param("userId") Long userId,
            Class<T> type
    );
}
