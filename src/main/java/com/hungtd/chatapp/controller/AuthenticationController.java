package com.hungtd.chatapp.controller;

import com.hungtd.chatapp.dto.request.AuthenticationRequest;
import com.hungtd.chatapp.dto.request.IntrospectRequest;
import com.hungtd.chatapp.dto.request.LogoutRequest;
import com.hungtd.chatapp.dto.request.RefreshTokenRequest;
import com.hungtd.chatapp.dto.response.ApiResponse;
import com.hungtd.chatapp.dto.response.AuthenticationResponse;
import com.hungtd.chatapp.dto.response.IntrospectResponse;
import com.hungtd.chatapp.service.auth.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/log-in")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        AuthenticationResponse result = authenticationService.authenticate(request);

        if (result.isAuthenticated()) {
            return ResponseEntity.status(200).body(
                    ApiResponse.<AuthenticationResponse>builder()
                            .data(result)
                            .build()
            );
        }

        return ResponseEntity.status(401).body(
                ApiResponse.<AuthenticationResponse>builder()
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody @Valid IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);

        return ResponseEntity.status(200).body(
                ApiResponse.<IntrospectResponse>builder()
                        .data(result)
                        .build()
        );
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.refreshToken(request);

        return ResponseEntity.status(200).body(
                ApiResponse.<AuthenticationResponse>builder()
                        .data(authenticationResponse)
                        .build()
        );
    }

    @PostMapping("/log-out")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody @Valid LogoutRequest request) {
        authenticationService.logout(request);

        return ResponseEntity.status(200).body(
                ApiResponse.<Void>builder()
                        .message("Đăng xuất thành công!")
                        .build()
        );
    }
}
