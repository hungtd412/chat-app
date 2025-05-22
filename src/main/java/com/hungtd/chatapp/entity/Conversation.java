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
@Table(name = "conversation")
public class Conversation extends BaseEntity {
    @Id
    @Column(length = 36, name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(length = 40)
    String title;

    @Column(name = "creator_id", length = 36)
    String creatorId;
}
