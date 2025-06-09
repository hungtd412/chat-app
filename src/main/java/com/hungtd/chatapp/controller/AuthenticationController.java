package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.AuthenticationRequest;
import com.hungtd.chatapp.dto.request.IntrospectRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.AuthenticationResponse;
import com.hungtd.chatapp.dto.response.IntrospectResponse;
import com.hungtd.chatapp.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/log-in")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse result = authenticationService.authenticate(request);

        if (result.isAuthenticated()) {
            return new ApiResponse<AuthenticationResponse>()
                    .buildSuccessfulApiResponse(200, result);
        }

        return new ApiResponse<AuthenticationResponse>()
                .buildSuccessfulApiResponse(401, result);
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .data(result)
                .build();
    }
}
