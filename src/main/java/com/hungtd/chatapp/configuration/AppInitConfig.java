package com.hungtd.chatapp.configuration;

import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.Role;
import com.hungtd.chatapp.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            // Create admin user if not exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                User user = User.builder()
                        .email("admin@gm.con")
                        .username("admin")
                        .password(passwordEncoder.encode("123"))
                        .firstName("admin")
                        .lastName("admin")
                        .roles(new HashSet<>(Collections.singletonList(Role.ADMIN.name())))
                        .build();

                userRepository.save(user);
                log.warn("default admin created with admin/123");
            }
            
            // Create 5 default users if no regular users exist
            if (userRepository.count() <= 1) { // Only admin exists or no users
                List<User> defaultUsers = List.of(
                    User.builder()
                        .email("hung@example.com")
                        .username("hung")
                        .password(passwordEncoder.encode("123"))
                        .firstName("Hung")
                        .lastName("Nguyen")
                        .dob(LocalDate.of(1990, 1, 15))
                        .isActive(true)
                        .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                        .build(),
                    User.builder()
                        .email("trang@example.com")
                        .username("trang")
                        .password(passwordEncoder.encode("123"))
                        .firstName("Trang")
                        .lastName("Le")
                        .dob(LocalDate.of(1992, 5, 20))
                        .isActive(true)
                        .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                        .build(),
                    User.builder()
                        .email("thuy@example.com")
                        .username("thuy")
                        .password(passwordEncoder.encode("123"))
                        .firstName("Thuy")
                        .lastName("Pham")
                        .dob(LocalDate.of(1988, 9, 10))
                        .isActive(true)
                        .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                        .build(),
                    User.builder()
                        .email("vinh@example.com")
                        .username("vinh")
                        .password(passwordEncoder.encode("123"))
                        .firstName("Vinh")
                        .lastName("Tran")
                        .dob(LocalDate.of(1995, 3, 25))
                        .isActive(true)
                        .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                        .build(),
                    User.builder()
                        .email("nam@example.com")
                        .username("nam")
                        .password(passwordEncoder.encode("123"))
                        .firstName("Nam")
                        .lastName("Hoang")
                        .dob(LocalDate.of(1991, 7, 8))
                        .isActive(true)
                        .roles(new HashSet<>(Collections.singletonList(Role.USER.name())))
                        .build()
                );
                
                userRepository.saveAll(defaultUsers);
                log.warn("Created 5 default users: hung, trang, thuy, vinh, nam (all with password '123')");
            }
        };
    }
}
