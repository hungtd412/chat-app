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
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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

    @Column(name = "roles")
    Set<String> roles;

    @OneToMany(mappedBy = "sender")
    Set<FriendRequest> sentFriendRequests;

    @OneToMany(mappedBy = "receiver")
    Set<FriendRequest> receivedFriendRequests;
    
    @OneToMany(mappedBy = "user1")
    Set<Friend> friendsAsUser1;
    
    @OneToMany(mappedBy = "user2")
    Set<Friend> friendsAsUser2;
}