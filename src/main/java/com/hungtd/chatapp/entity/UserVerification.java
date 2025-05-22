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
@Table(name = "user_verification")
public class UserVerification {
    @Id
    @Column(name = "user_id", length = 36)
    String userId;

    @Column(name = "verification_code", length = 45, nullable = false)
    String verificationCode;

    @Column(name = "created_at", length = 45)
    String createdAt;
}
