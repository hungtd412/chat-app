package com.hungtd.chatapp.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${fe.url}")
    private String feUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling the SockJS fallback options so that
        // alternative messaging options can be used if WebSocket is not available.
        registry.addEndpoint("/ws")
                .setAllowedOrigins(feUrl)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple message broker to carry messages back to the client on destinations
        // prefixed with "/topic" and "/queue"
        // "/topic" is typically used for messages that are broadcasted to multiple clients
        // "/queue" is typically used for messages targeted at specific users
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Set prefix for messages bound for methods annotated with @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        
        // Set prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }
}
