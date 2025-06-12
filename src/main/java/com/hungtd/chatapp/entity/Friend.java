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
@Table(name = "friends")
@IdClass(FriendId.class)
public class Friend extends BaseEntity {
    
    @Id
    @Column(name = "user_id_1")
    Long userId1;
    
    @Id
    @Column(name = "user_id_2")
    Long userId2;
    
    @ManyToOne
    @JoinColumn(name = "user_id_1", referencedColumnName = "id", insertable = false, updatable = false)
    User user1;
    
    @ManyToOne
    @JoinColumn(name = "user_id_2", referencedColumnName = "id", insertable = false, updatable = false)
    User user2;
}
