package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.UserCreationRequest;
import com.hungtd.chatapp.dto.request.UserUpdateRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.UserResponse;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@ResponseBody convert java object to json object
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    /*
    @RequestBody converts json object to UserCreationRequest
    @Valid requires system to validate user's input based on validation rules
        in UserCreationRequest class
     */
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(userCreationRequest));
        apiResponse.setCode(1001);
        return apiResponse;
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }


    @GetMapping("/{id}")
    public UserResponse get(@PathVariable("id") String id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable("id") String id, @RequestBody UserUpdateRequest userUpdateRequest) {
        return userService.updateUser(id, userUpdateRequest);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable("id") String id) {
        userService.deleteUser(id);
        return "Delete success";
    }
}
