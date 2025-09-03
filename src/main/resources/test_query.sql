WITH msgs AS (SELECT m.id,
                     m.conversation_id,
                     m.message,
                     m.created_at,
                     ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.id DESC) AS rn
              FROM messages m)
SELECT c.id,
       c.title,
       c.image_url,
       c.type,
       p2.friend_name,
       p2.avt_url,
       m.message as latest_message,
       m.created_at
FROM (select id, image_url, title, type from conversations) c
         JOIN (select conversation_id, user_id from participants) p
              ON p.conversation_id = c.id AND p.user_id = 2
         LEFT JOIN (select user_id, avt_url, conversation_id,
                      CONCAT(first_name, ' ', last_name) as friend_name
               from participants
                        join users on participants.user_id = users.id) p2
              ON p2.conversation_id = c.id AND p2.user_id <> 2 AND type = 'PRIVATE'
         LEFT JOIN msgs m
                   ON m.conversation_id = c.id AND m.rn = 1 /*first row of each message partition*/
ORDER BY COALESCE(m.id, 0) DESC;

desc messages;


-- select messages from a specific message id(offset) backward

SELECT *
FROM messages m
WHERE m.conversation_id = 1
  AND (
    (0 > 0 AND m.id < 0)
        OR (0 <= 0)
    )
ORDER BY m.id DESC
LIMIT 2;

SELECT *
FROM messages m where m.id = 167;


