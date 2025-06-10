package com.hungtd.chatapp.service.auth;

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
public class AuthenticationService {

    JwtService jwtService;
    TokenBlacklistService tokenBlacklistService;
    UserRepository userRepository;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        boolean isValid = true;

        try {
            jwtService.verifyToken(request.getToken()); // will throw error if invalid token
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
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(authenticated)
                .build();
    }

    public void logout(LogoutRequest request) {
        String token = request.getToken();

        try {
            // Validate the token is correctly formatted
            jwtService.verifyToken(token);

            // Get token expiry time directly from JWT service
            Long expiryTimeMs = jwtService.getTokenExpirationTime(token);
            tokenBlacklistService.blacklistToken(token, expiryTimeMs);
        } catch (Exception e) {
            log.warn("Invalid token provided for logout: {}", e.getMessage());
        }
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request){
        String accessToken = request.getAccessToken();
        String refreshToken = request.getRefreshToken();
        
        try {
            // First, validate refresh token (this will throw exception if invalid)
            jwtService.verifyRefreshToken(refreshToken);
            
            // If we get here, refresh token is valid, now validate the structure of access token 
            // (but don't error on blacklisted/expired)
            try {
                jwtService.verifyToken(accessToken);
            } catch (AppException e) {
                // Only proceed if the token was blacklisted or expired
                if (e.getErrorCode() != ErrorCode.UNAUTHENTICATED && 
                    e.getErrorCode() != ErrorCode.INVALID_KEY) {
                    throw e;
                }
            }
            
            // Blacklist the old access token regardless of its validity
            Long accessTokenExpiryMs = jwtService.getTokenExpirationTime(accessToken);
            if (accessTokenExpiryMs != null) {
                tokenBlacklistService.blacklistToken(accessToken, accessTokenExpiryMs);
            }
            
            // Get username from refresh token and generate a new access token
            String username = jwtService.verifyRefreshToken(refreshToken).getJWTClaimsSet().getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user);
            
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
