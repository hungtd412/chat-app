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
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @Column(length = 36, name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(length = 16, nullable = false, unique = true)
    String phone;

    @Column(length = 255, nullable = false, unique = true)
    String email;

    @Column(length = 40, nullable = false)
    String password;

    @Column(name = "first_name", length = 20, nullable = false)
    String firstName;

    @Column(name = "last_name", length = 20, nullable = false)
    String lastName;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "is_blocked")
    Boolean isBlocked;

    @Column(columnDefinition = "TEXT")
    String preferences;
}
