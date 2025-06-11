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
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    JwtServiceImpl jwtServiceImpl;
    TokenBlacklistServiceImpl tokenBlacklistServiceImpl;
    UserRepository userRepository;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        boolean isValid = true;

        try {
            jwtServiceImpl.verifyToken(request.getToken()); // will throw error if invalid token
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
        String accessToken = jwtServiceImpl.generateAccessToken(user);
        String refreshToken = jwtServiceImpl.generateRefreshToken(user);

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
                    Long accessTokenExpiryMs = jwtServiceImpl.getTokenExpirationTime(accessToken);
                    if (accessTokenExpiryMs != null) {
                        // Blacklist the access token
                        tokenBlacklistServiceImpl.blacklistToken(accessToken, accessTokenExpiryMs);
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
                    Long refreshTokenExpiryMs = jwtServiceImpl.getTokenExpirationTime(refreshToken);
                    if (refreshTokenExpiryMs != null) {
                        // Blacklist the refresh token
                        tokenBlacklistServiceImpl.blacklistToken(refreshToken, refreshTokenExpiryMs);
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

    public AuthenticationResponse refreshToken(RefreshTokenRequest request){
        String accessToken = request.getAccessToken();
        String refreshToken = request.getRefreshToken();
        
        try {
            // First, validate refresh token (this will throw exception if invalid)
            jwtServiceImpl.verifyRefreshToken(refreshToken);
            
            // If we get here, refresh token is valid, now validate the structure of access token 
            // (but don't error on blacklisted/expired)
            try {
                jwtServiceImpl.verifyToken(accessToken);
            } catch (AppException e) {
                // Only proceed if the token was blacklisted or expired
                if (e.getErrorCode() != ErrorCode.UNAUTHENTICATED && 
                    e.getErrorCode() != ErrorCode.INVALID_KEY) {
                    throw e;
                }
            }
            
            // Blacklist the old access token regardless of its validity
            Long accessTokenExpiryMs = jwtServiceImpl.getTokenExpirationTime(accessToken);
            if (accessTokenExpiryMs != null) {
                tokenBlacklistServiceImpl.blacklistToken(accessToken, accessTokenExpiryMs);
            }
            
            // Get username from refresh token and generate a new access token
            String username = jwtServiceImpl.verifyRefreshToken(refreshToken).getJWTClaimsSet().getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // Generate new access token
            String newAccessToken = jwtServiceImpl.generateAccessToken(user);
            
            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Keep the same refresh token
                    .authenticated(true)
                    .build();
            
        } catch (Exception e) {
            // If refresh token is invalid or expired
            log.error("Error refreshing token: {}", e.getMessage());
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }
    }
}