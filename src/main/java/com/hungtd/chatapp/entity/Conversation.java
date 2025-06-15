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
@Table(name = "conversations")
public class Conversation {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 40)
    String title;

    @Column(name = "image_url", columnDefinition = "TEXT")
    String imageUrl;

    @Column(name = "cloudinary_image_id", columnDefinition = "TEXT")
    String cloudinaryImageId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 8, nullable = false)
    Type type;
    
    public enum Type {
        PRIVATE, GROUP
    }
}
