package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.UserCreationRequest;
import com.hungtd.chatapp.dto.request.UpdateNameDobRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.UploadImageResponse;
import com.hungtd.chatapp.dto.response.UserResponse;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.mapper.UserMapper;
import com.hungtd.chatapp.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
//@RestController = @Controller + @ResponseBody convert java object to json object
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;
    UserMapper userMapper;

    /*
    @RequestBody converts json object to UserCreationRequest
    @Valid requires system to validate user's input based on validation rules
        in UserCreationRequest class
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        User user = userService.create(userCreationRequest);

        return ResponseEntity.status(201).body(
                ApiResponse.<UserResponse>builder()
                        .data(userMapper.toUserResponse(user))
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        List<User> userList = userService.getAll();

        List<UserResponse> userResponseList = userList.stream()
                .map(userMapper::toUserResponse)
                .toList();

        return ResponseEntity.status(200).body(
                ApiResponse.<List<UserResponse>>builder()
                        .data(userResponseList)
                        .build()
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> profile() {
        User user = userService.currentUser();

        return ResponseEntity.status(200).body(
                ApiResponse.<UserResponse>builder()
                        .data(userMapper.toUserResponse(user))
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable("id") Long id) {
        User user = userService.get(id);

        return ResponseEntity.status(200).body(
                ApiResponse.<UserResponse>builder()
                        .data(userMapper.toUserResponse(user))
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        userService.delete(id);

        return ResponseEntity.status(200).body(
                ApiResponse.<Void>builder()
                        .build()
        );
    }

    @PatchMapping(value = "/me/avatar")
    public ResponseEntity<ApiResponse<UploadImageResponse>> updateAvatar(@RequestPart("image") MultipartFile image) {
        UploadImageResponse response = userService.updateAvatar(image);

        return ResponseEntity.ok(
                ApiResponse.<UploadImageResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateNameDob(@RequestBody @Valid UpdateNameDobRequest updateNameDobRequest) {
        User user = userService.updateNameDob(updateNameDobRequest);

        return ResponseEntity.status(200).body(
                ApiResponse.<UserResponse>builder()
                        .data(userMapper.toUserResponse(user))
                        .build()
        );
    }

    @DeleteMapping(value = "/me/avatar")
    public ResponseEntity<ApiResponse<UploadImageResponse>> restoreDefaultAvatar() {
        userService.restoreDefaultAvatar();

        return ResponseEntity.ok(
                ApiResponse.<UploadImageResponse>builder().build()
        );
    }
}