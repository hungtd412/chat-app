package com.hungtd.chatapp.service.auth.impl;

import com.hungtd.chatapp.dto.request.AuthenticationRequest;
import com.hungtd.chatapp.dto.request.IntrospectRequest;
import com.hungtd.chatapp.dto.request.LogoutRequest;
import com.hungtd.chatapp.dto.request.RefreshTokenRequest;
import com.hungtd.chatapp.dto.response.AuthenticationResponse;
import com.hungtd.chatapp.dto.response.IntrospectResponse;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.auth.AuthenticationService;
import com.hungtd.chatapp.service.auth.TokenBlacklistService;
import com.hungtd.chatapp.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    JwtUtil jwtUtil;
    TokenBlacklistService tokenBlacklistService;
    UserRepository userRepository;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        boolean isValid = true;

        try {
            jwtUtil.verifyToken(request.getToken()); // will throw error if invalid token
        } catch (AppException appException) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Generate both access token and refresh token
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(authenticated)
                .build();
    }

    public void logout(LogoutRequest request) {
        String accessToken = request.getAccessToken();
        String refreshToken = request.getRefreshToken();

        try {
            // Blacklist access token if provided
            if (accessToken != null && !accessToken.isEmpty()) {
                try {
                    // Don't verify against blacklist - just get expiration time
                    Long accessTokenExpiryMs = jwtUtil.getTokenExpirationTime(accessToken);
                    if (accessTokenExpiryMs != null) {
                        // Blacklist the access token
                        tokenBlacklistService.blacklistToken(accessToken, accessTokenExpiryMs);
                        log.info("Access token blacklisted successfully");
                    }
                } catch (Exception e) {
                    log.warn("Could not blacklist access token: {}", e.getMessage());
                }
            }

            // Blacklist refresh token if provided
            if (refreshToken != null && !refreshToken.isEmpty()) {
                try {
                    // Get expiration time for refresh token
                    Long refreshTokenExpiryMs = jwtUtil.getTokenExpirationTime(refreshToken);
                    if (refreshTokenExpiryMs != null) {
                        // Blacklist the refresh token
                        tokenBlacklistService.blacklistToken(refreshToken, refreshTokenExpiryMs);
                        log.info("Refresh token blacklisted successfully");
                    }
                } catch (Exception e) {
                    log.warn("Could not blacklist refresh token: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Error during logout: {}", e.getMessage());
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String accessToken = Optional.ofNullable(request.getAccessToken())
                .filter(token -> !token.isBlank())
                .orElseThrow(() -> new AppException(ErrorCode.EMPTY_ACCESSTOKEN));

        String refreshToken = Optional.ofNullable(request.getRefreshToken())
                .filter(token -> !token.isBlank())
                .orElseThrow(() -> new AppException(ErrorCode.EMPTY_REFRESHTOKEN));

        try {
            var refreshJwt = jwtUtil.verifyRefreshToken(refreshToken);

            try {
                jwtUtil.verifyToken(accessToken);
            } catch (AppException e) {
                if (e.getErrorCode() != ErrorCode.UNAUTHENTICATED
                        && e.getErrorCode() != ErrorCode.SESSION_EXPIRED
                        && e.getErrorCode() != ErrorCode.INVALID_KEY) {
                    throw e;
                }
            }

            Long accessTokenExpiryMs = jwtUtil.getTokenExpirationTime(accessToken);
            if (accessTokenExpiryMs != null) {
                tokenBlacklistService.blacklistToken(accessToken, accessTokenExpiryMs);
            }

            Long userId = Long.parseLong(refreshJwt.getJWTClaimsSet().getSubject());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            String newAccessToken = jwtUtil.generateAccessToken(user);

            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .authenticated(true)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (ParseException | NumberFormatException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_KEY);
        }
    }
}