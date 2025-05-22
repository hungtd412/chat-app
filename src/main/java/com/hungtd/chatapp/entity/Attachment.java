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
@Table(name = "attachments")
public class Attachment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    String id;

    @Column(name = "messages_id")
    String messagesId;

    @Column(name = "thumb_url", length = 45)
    String thumbUrl;

    @Column(name = "file_url", length = 45)
    String fileUrl;
}
