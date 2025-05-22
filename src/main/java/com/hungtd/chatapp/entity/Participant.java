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
public class Participant extends BaseEntity {
    @Id
    @Column(length = 36, name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "conversation_id", length = 36)
    String conversationId;

    @Column(name = "users_id", length = 36)
    String usersId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    Type type;

    public enum Type {
        TYPE1, TYPE2
    }
}
