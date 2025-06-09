package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.UserCreationRequest;
import com.hungtd.chatapp.dto.request.UserUpdateRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.UserResponse;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.mapper.UserMapper;
import com.hungtd.chatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        User user = userService.createUser(userCreationRequest);

        return new ApiResponse<UserResponse>()
                .buildSuccessfulApiResponse(
                        201,
                        userMapper.toUserResponse(user));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAll() {
        List<User> userList = userService.getAll();
        return new ApiResponse<List<UserResponse>>().buildSuccessfulApiResponse(
                200,
                userList.stream()
                        .map(userMapper::toUserResponse)
                        .toList());
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> profile() {
        User user = userService.profile();
        return new ApiResponse<UserResponse>()
                .buildSuccessfulApiResponse(
                        200,
                        userMapper.toUserResponse(user));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable("id") String id) {
        User user = userService.getUser(id);
        return new ApiResponse<UserResponse>()
                .buildSuccessfulApiResponse(
                        200,
                        userMapper.toUserResponse(user));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable("id") String id, @RequestBody UserUpdateRequest userUpdateRequest) {
        User user = userService.updateUser(id, userUpdateRequest);

        return new ApiResponse<UserResponse>()
                .buildSuccessfulApiResponse(
                        200,
                        userMapper.toUserResponse(user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteUser(@PathVariable("id") String id) {
        userService.deleteUser(id);
        return new ApiResponse<Object>().buildSuccessfulApiResponse(200, new Object());
    }
}
