package com.hungtd.chatapp.service.auth.impl;

import com.hungtd.chatapp.service.auth.TokenBlacklistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    // Using Redis for distributed token blacklisting
    RedisTemplate<String, String> redisTemplate;  // Spring's RedisTemplate to interact with Redis database

    private static final String TOKEN_PREFIX = "blacklisted_token:";  // Prefix for Redis keys to avoid key collisions

    public void blacklistToken(String token, Long expiryTimeMs) {
        // Calculate TTL in seconds
        long ttlSeconds = (expiryTimeMs - System.currentTimeMillis()) / 1000;  // Convert milliseconds to seconds
        if (ttlSeconds > 0) {  // Only store if token hasn't already expired
            // Store token in Redis with automatic expiration
            redisTemplate.opsForValue().set(
                TOKEN_PREFIX + token,  // Key with prefix to identify as blacklisted token
                "1",  // Simple value - we only care about the key's existence
                ttlSeconds,  // Time until automatic deletion from Redis
                TimeUnit.SECONDS  // Time unit for the TTL
            );
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));  // Check if token exists in Redis
    }

    // No need for explicit cleanup as Redis handles expiry automatically
}