package com.hungtd.chatapp.util;

import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.service.auth.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(tokenBlacklistService);
        ReflectionTestUtils.setField(jwtUtil, "SIGNER_KEY",
                "1234567890123456789012345678901234567890123456789012345678901234");
    }

    @Test
    void verifyToken_shouldPass_whenTokenValidAndNotBlacklisted() {
        User user = buildUser(1L, "testuser");
        String accessToken = jwtUtil.generateAccessToken(user);

        when(tokenBlacklistService.isBlacklisted(accessToken)).thenReturn(false);

        assertDoesNotThrow(() -> jwtUtil.verifyToken(accessToken));
    }

    @Test
    void verifyToken_shouldThrowUnauthenticated_whenTokenBlacklisted() {
        User user = buildUser(1L, "testuser");
        String accessToken = jwtUtil.generateAccessToken(user);

        when(tokenBlacklistService.isBlacklisted(accessToken)).thenReturn(true);

        AppException appException = assertThrows(AppException.class, () -> jwtUtil.verifyToken(accessToken));
        assertEquals(ErrorCode.UNAUTHENTICATED, appException.getErrorCode());
    }

    @Test
    void verifyRefreshToken_shouldThrowInvalidTokenType_whenAccessTokenProvided() {
        User user = buildUser(1L, "testuser");
        String accessToken = jwtUtil.generateAccessToken(user);

        when(tokenBlacklistService.isBlacklisted(accessToken)).thenReturn(false);

        AppException appException = assertThrows(AppException.class, () -> jwtUtil.verifyRefreshToken(accessToken));
        assertEquals(ErrorCode.INVALID_TOKEN_TYPE, appException.getErrorCode());
    }

    private User buildUser(Long id, String username) {
        return User.builder()
                .id(id)
                .email(username + "@mail.com")
                .username(username)
                .password("encoded")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .isBlocked(false)
                .roles(Set.of("USER"))
                .build();
    }
}
