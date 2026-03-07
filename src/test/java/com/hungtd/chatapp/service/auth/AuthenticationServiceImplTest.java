package com.hungtd.chatapp.service.auth;

import com.hungtd.chatapp.dto.request.AuthenticationRequest;
import com.hungtd.chatapp.dto.request.LogoutRequest;
import com.hungtd.chatapp.dto.request.RefreshTokenRequest;
import com.hungtd.chatapp.dto.response.AuthenticationResponse;
import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.repository.UserRepository;
import com.hungtd.chatapp.service.auth.impl.AuthenticationServiceImpl;
import com.hungtd.chatapp.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private UserRepository userRepository;

    private JwtUtil jwtUtil;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(tokenBlacklistService);
        ReflectionTestUtils.setField(jwtUtil, "SIGNER_KEY",
                "1234567890123456789012345678901234567890123456789012345678901234");

        authenticationService = new AuthenticationServiceImpl(jwtUtil, tokenBlacklistService, userRepository);
    }

    @Test
    void authenticate_shouldReturnAccessAndRefreshToken_whenCredentialsValid() {
        User user = buildUser(1L, "testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        AuthenticationResponse response = authenticationService.authenticate(
                AuthenticationRequest.builder().username("testuser").password("password123").build());

        assertTrue(response.isAuthenticated());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void authenticate_shouldThrowUnauthenticated_whenPasswordInvalid() {
        User user = buildUser(1L, "testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        AppException appException = assertThrows(AppException.class, () -> authenticationService.authenticate(
                AuthenticationRequest.builder().username("testuser").password("wrong-pass").build()));

        assertEquals(ErrorCode.UNAUTHENTICATED, appException.getErrorCode());
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenValid() {
        User user = buildUser(1L, "1", "password123");
        when(userRepository.findByUsername("1")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);

        AuthenticationResponse auth = authenticationService.authenticate(
                AuthenticationRequest.builder().username("1").password("password123").build());

        AuthenticationResponse refreshed = authenticationService.refreshToken(
                RefreshTokenRequest.builder()
                        .accessToken(auth.getAccessToken())
                        .refreshToken(auth.getRefreshToken())
                        .build());

        assertTrue(refreshed.isAuthenticated());
        assertNotNull(refreshed.getAccessToken());
        assertEquals(auth.getRefreshToken(), refreshed.getRefreshToken());
    }

    @Test
    void refreshToken_shouldThrowInvalidTokenType_whenAccessTokenUsedAsRefreshToken() {
        User user = buildUser(1L, "1", "password123");
        when(userRepository.findByUsername("1")).thenReturn(Optional.of(user));
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);

        AuthenticationResponse auth = authenticationService.authenticate(
                AuthenticationRequest.builder().username("1").password("password123").build());

        AppException appException = assertThrows(AppException.class, () -> authenticationService.refreshToken(
                RefreshTokenRequest.builder()
                        .accessToken(auth.getAccessToken())
                        .refreshToken(auth.getAccessToken())
                        .build()));

        assertEquals(ErrorCode.INVALID_TOKEN_TYPE, appException.getErrorCode());
    }

    @Test
    void logout_shouldBlacklistBothTokens_whenTokensProvided() {
        User user = buildUser(1L, "1", "password123");
        when(userRepository.findByUsername("1")).thenReturn(Optional.of(user));

        AuthenticationResponse auth = authenticationService.authenticate(
                AuthenticationRequest.builder().username("1").password("password123").build());

        authenticationService.logout(new LogoutRequest(
                auth.getAccessToken(),
                auth.getRefreshToken()));

        verify(tokenBlacklistService, atLeastOnce()).blacklistToken(anyString(), anyLong());
    }

    private User buildUser(Long id, String username, String rawPassword) {
        return User.builder()
                .id(id)
                .email(username + "@mail.com")
                .username(username)
                .password(new BCryptPasswordEncoder(10).encode(rawPassword))
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .isBlocked(false)
                .roles(Set.of("USER"))
                .build();
    }
}
