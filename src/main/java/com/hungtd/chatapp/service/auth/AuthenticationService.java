package com.hungtd.chatapp.service.auth;

import com.hungtd.chatapp.dto.request.AuthenticationRequest;
import com.hungtd.chatapp.dto.request.IntrospectRequest;
import com.hungtd.chatapp.dto.request.LogoutRequest;
import com.hungtd.chatapp.dto.response.AuthenticationResponse;
import com.hungtd.chatapp.dto.response.IntrospectResponse;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticationService {

    JwtService jwtService;
    TokenBlacklistService tokenBlacklistService;
    UserRepository userRepository;

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            jwtService.verifyToken(request.getToken()); //will throw error if invalid token
        } catch(AppException appException) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }

        return IntrospectResponse.builder()
                .valid(true)
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

        String token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
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
            if (expiryTimeMs != null) {
                tokenBlacklistService.blacklistToken(token, expiryTimeMs);
            }
        } catch (Exception e) {
            log.warn("Invalid token provided for logout: {}", e.getMessage());
        }
    }
}
