package com.hungtd.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.util.Set;

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

    @Column(length = 255, nullable = false, unique = true)
    String email;

    @Column(length = 16, nullable = false, unique = true)
    String username;

    @Column(length = 255, nullable = false)
    String password;

    @Column(name = "first_name", length = 20, nullable = false)
    String firstName;

    @Column(name = "last_name", length = 20, nullable = false)
    String lastName;

    @Column(name = "dob")
    LocalDate dob;

    @Column(name = "is_active", columnDefinition = "boolean default false")
    Boolean isActive = false;

    @Column(name = "is_blocked", columnDefinition = "boolean default false")
    Boolean isBlocked = false;

    @Column(columnDefinition = "TEXT")
    String preferences;

//    @Transient //not map this attribute to table
    @Column(name = "roles")
    Set<String> roles;
}