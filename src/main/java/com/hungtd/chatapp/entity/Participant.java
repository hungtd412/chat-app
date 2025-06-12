package com.hungtd.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "participants")
@IdClass(ParticipantId.class)
public class Participant {

    @Id
    @Column(name = "conversation_id")
    String conversationId;

    @Id
    @Column(name = "user_id")
    String userId;
    
    @ManyToOne
    @JoinColumn(name = "conversation_id", referencedColumnName = "id", insertable = false, updatable = false)
    Conversation conversation;
    
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    Type type;

    public enum Type {
        ADMIN, MEMBER
    }
}
