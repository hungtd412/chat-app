package com.hungtd.chatapp.service.auth;

public interface TokenBlacklistService {
    void blacklistToken(String token, Long expiryTimeMs);
    boolean isBlacklisted(String token);
}
