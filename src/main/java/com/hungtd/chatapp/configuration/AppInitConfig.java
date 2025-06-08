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

import java.util.Collections;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor //autowire non-null(@NonNull) and final field
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
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
        };
    }
}
