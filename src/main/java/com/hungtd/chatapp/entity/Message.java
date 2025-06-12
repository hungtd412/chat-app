package com.hungtd.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "messages")
public class Message extends BaseEntity {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id")
    Long conversationId;

    @Column(name = "sender_id")
    Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 10)
    Type messageType;

    @Column(length = 255)
    String message;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    public enum Type {
        TYPE1, TYPE2, TYPE3, TYPE4
    }
}