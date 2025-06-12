package com.hungtd.chatapp.repository;

import com.hungtd.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String s);
    boolean existsByUsername(String s);
    Optional<User> findByEmail(String s);
    Optional<User> findByUsername(String s);

}