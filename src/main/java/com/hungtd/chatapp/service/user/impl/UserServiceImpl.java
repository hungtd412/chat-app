package com.hungtd.chatapp.service.user.impl;

import com.hungtd.chatapp.configuration.CloudinaryConfig;
import com.hungtd.chatapp.dto.request.UserCreationRequest;
import com.hungtd.chatapp.dto.request.UserUpdateRequest;
import com.hungtd.chatapp.dto.response.CloudinaryResponse;
import com.hungtd.chatapp.dto.response.UploadImageResponse;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.Role;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.mapper.UserMapper;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.cloudinary.CloudinaryService;
import com.hungtd.chatapp.service.user.UserService;
import com.hungtd.chatapp.util.FileUploadUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor //autowire non-null(@NonNull) and final field
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    CloudinaryService cloudinaryService;

    @Override
    public User create(UserCreationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAvtUrl(CloudinaryConfig.CLOUDINARY_DEFAULT_AVATAR_URL);
        user.setCloudinaryAvtId(CloudinaryConfig.CLOUDINARY_DEFAULT_AVATAR_PUBLICID);

        Set<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );

        return user;
    }

    @Override
    @PostAuthorize("returnObject.getUsername() == authentication.getName() or hasRole(\"ADMIN\")")
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );
    }

    @Override
    public User update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );
        userMapper.updateUser(user, request);

        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );
        userRepository.deleteById(user.getId());
    }

    @Override
    public boolean isExistById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public UploadImageResponse uploadAvatar(MultipartFile image) {
        final User currentUser = currentUser();

        FileUploadUtil.assertAllowed(image, FileUploadUtil.IMAGE_PATTERN);


        if (StringUtils.isNotBlank(currentUser.getCloudinaryAvtId())
                && !(Objects.equals(currentUser.getCloudinaryAvtId(),
                CloudinaryConfig.CLOUDINARY_DEFAULT_AVATAR_PUBLICID))
        ) {
            cloudinaryService.delete(currentUser.getCloudinaryAvtId());
            log.info("Previous avatar for user {} will be overwritten", currentUser.getUsername());
        }

        // Upload new image
        final CloudinaryResponse cloudinaryResponse = cloudinaryService.uploadFile(image, "avatar");

        // Update user profile with the new avatar URL
        currentUser.setAvtUrl(cloudinaryResponse.getUrl());
        currentUser.setCloudinaryAvtId(cloudinaryResponse.getPublicId());
        userRepository.save(currentUser);

        // Return response
        return UploadImageResponse.builder()
                .imageUrl(cloudinaryResponse.getUrl())
                .cloudinaryId(cloudinaryResponse.getPublicId())
                .build();
    }
}