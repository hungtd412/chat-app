package com.hungtd.chatapp.configuration;

import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.util.JwtUtil;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    JwtUtil jwtUtil;

//    @Autowired
//    UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        final StompCommand command = accessor.getCommand();
        
        if (StompCommand.CONNECT == command || StompCommand.SEND == command) {
            try {
                String jwt = jwtUtil.extractTokenFromStompHeader(accessor);
                SignedJWT principal = jwtUtil.verifyToken(jwt);
//                UserDetails userDetails = userDetailsService.loadUserByUsername()

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal.getJWTClaimsSet().getSubject(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                
                accessor.setUser(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        
        return message;
    }
}
