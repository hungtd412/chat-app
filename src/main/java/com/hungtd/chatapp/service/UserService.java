package com.hungtd.chatapp.service;

import com.hungtd.chatapp.dto.request.UserCreationRequest;
import com.hungtd.chatapp.dto.request.UserUpdateRequest;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.Role;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.mapper.UserMapper;
import com.hungtd.chatapp.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor //autowire non-null(@NonNull) and final field
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public User createUser(UserCreationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

        user.setRoles(roles);

        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User profile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );

        return user;
    }

    @PostAuthorize("returnObject.getUsername() == authentication.getName() or hasRole(\"ADMIN\")")
    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );
    }

    public User updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTED)
                );
        userMapper.updateUser(user, request);

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );
        userRepository.deleteById(user.getId());
    }

}
