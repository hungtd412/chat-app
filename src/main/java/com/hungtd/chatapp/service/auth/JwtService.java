package com.hungtd.chatapp.service.auth;

import com.hungtd.chatapp.entity.User;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    SignedJWT verifyToken(String token);
    SignedJWT verifyRefreshToken(String token);
    Long getTokenExpirationTime(String token);
    String buildScope(User user);
    Long extractUserIdByTokenStompHeader(StompHeaderAccessor headerAccessor);
    String extractTokenFromStompHeader(StompHeaderAccessor headerAccessor);
    String extractUserIdFromToken(String token);
}
