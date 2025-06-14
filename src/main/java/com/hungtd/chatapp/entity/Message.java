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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    Conversation conversation;

    @Column(name = "sender_id")
    Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10)
    Type type;

    @Column(columnDefinition = "TEXT")
    String message;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    public enum Type {
        TEXT, THUMB, FILE
    }
}
