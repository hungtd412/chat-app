package com.hungtd.chatapp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
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
        // alternative messaging options can be used if WebSocketService is not available.
        registry.addEndpoint("/ws")
                .setAllowedOrigins(feUrl)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Set prefix for messages bound for methods annotated with @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");

        // Set prefix for user-specific messages
        registry.setUserDestinationPrefix("/user11");

        //web socket like a bridge, it can provide us with a lot of bridges(ex: user11, user849, topic, queue,...)
        //user subscribe to receive message by using the bridge called user11

        //however we need someone to control the flow of messages,
        //this is where the message broker comes in

        // this bridge need a controller who can control and decide whom to send message to
        //this controller is simpleBroker.

        // As a result, we said to spring websocket that let's bring a simple broker to the bridge named user11.

        // this simple broker are
        //responsible for handling messages of user11 bridge(respectively the line below)
        registry.enableSimpleBroker("/user11");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor());
        WebSocketMessageBrokerConfigurer.super.configureClientInboundChannel(registration);
    }

    @Bean
    public WebSocketAuthInterceptor authInterceptor() {
        return new WebSocketAuthInterceptor();
    }

//    @Bean
//    AuthorizationManager<Message<?>> messageAuthorizationManager(
//            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
//
//        messages
//                // Only authenticated users can connect
//                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.MESSAGE).authenticated()
//
//                // Allow subscribing/sending to "/app/**" only if user has ROLE_USER
//                .simpDestMatchers("/app/**").hasRole("USER")
//
//                // Allow subscriptions to private queues (/user11/**) only if authenticated
//                .simpDestMatchers("/user11/**").authenticated()
//
//                // Deny everything else unless explicitly allowed
//                .anyMessage().permitAll();
//
//        return messages.build();
//    }
}
