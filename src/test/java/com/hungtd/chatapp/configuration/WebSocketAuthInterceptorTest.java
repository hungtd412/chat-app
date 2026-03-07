package com.hungtd.chatapp.configuration;

import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.util.JwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebSocketAuthInterceptorTest {

    private WebSocketAuthInterceptor interceptor;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor();
        jwtUtil = mock(JwtUtil.class);
        ReflectionTestUtils.setField(interceptor, "jwtUtil", jwtUtil);
    }

    @Test
    void preSend_shouldSetAuthenticatedUser_whenConnectTokenValid() throws Exception {
        SignedJWT signedJWT = mock(SignedJWT.class);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("10").build();

        when(jwtUtil.extractTokenFromStompHeader(any())).thenReturn("valid-token");
        when(jwtUtil.verifyToken("valid-token")).thenReturn(signedJWT);
        when(signedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer valid-token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resultAccessor.getUser());
        assertEquals("10", resultAccessor.getUser().getName());
    }

    @Test
    void preSend_shouldThrowUnauthenticated_whenConnectTokenInvalid() {
        when(jwtUtil.extractTokenFromStompHeader(any())).thenReturn("invalid-token");
        when(jwtUtil.verifyToken("invalid-token")).thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer invalid-token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        AppException appException = assertThrows(AppException.class, () -> interceptor.preSend(message, null));
        assertEquals(ErrorCode.UNAUTHENTICATED, appException.getErrorCode());
    }
}
