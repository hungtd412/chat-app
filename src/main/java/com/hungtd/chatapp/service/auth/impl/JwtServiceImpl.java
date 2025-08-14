package com.hungtd.chatapp.service.auth.impl;

import com.hungtd.chatapp.entity.User;
import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.exception.AppException;
import com.hungtd.chatapp.service.auth.JwtService;
import com.hungtd.chatapp.service.auth.TokenBlacklistService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JwtServiceImpl implements JwtService {
    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    // Constants for token types
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    private final TokenBlacklistService tokenBlacklistService;

    // Generate an access token with short expiration (1 hour)
    @Override
    public String generateAccessToken(User user) {
        return generateJwtToken(user, ACCESS_TOKEN, 1, ChronoUnit.HOURS);
    }

    // Generate a refresh token with longer expiration (7 days)
    public String generateRefreshToken(User user) {
        return generateJwtToken(user, REFRESH_TOKEN, 7, ChronoUnit.DAYS);
    }

    // Common method to generate tokens with different types and expiration times
    private String generateJwtToken(User user, String tokenType, long amount, ChronoUnit unit) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .issuer("hung")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(amount, unit).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim(TOKEN_TYPE_CLAIM, tokenType) // Add token type claim
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    // Verify any token (access or refresh)
    public SignedJWT verifyToken(String token) throws RuntimeException {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verified = signedJWT.verify(verifier);
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (!(verified && expiryTime.after(new Date()))) {
                throw new AppException(ErrorCode.INVALID_KEY);
            }

            if (tokenBlacklistService.isBlacklisted(token)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return signedJWT;
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // Specifically verify a refresh token
    public SignedJWT verifyRefreshToken(String token) throws RuntimeException {
        try {
            // Step 1: First validate this is a valid token (checks signature, expiration, and blacklist status)
            SignedJWT signedJWT = verifyToken(token);
            
            // Step 2: Extract the token type claim from the JWT payload
            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim(TOKEN_TYPE_CLAIM);
            
            // Step 3: Verify this is specifically a refresh token, not an access token
            if (!REFRESH_TOKEN.equals(tokenType)) {
                // If token type is not "refresh", throw an exception - prevents using access tokens for refresh operations
                throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
            }
            
            // Step 4: Return the validated refresh token for further processing
            return signedJWT;
        } catch (ParseException e) {
            // Handle parsing errors (malformed JWT)
            throw new RuntimeException(e);
        }
    }

    // Get token expiration time
    public Long getTokenExpirationTime(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null ? expirationTime.getTime() : null;
        } catch (ParseException e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }

    // Build scopes from user roles
    public String buildScope(User user) {
        StringJoiner rolesStringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(rolesStringJoiner::add);

        return rolesStringJoiner.toString();
    }

    @Override
    public Long extractUserIdByTokenStompHeader(StompHeaderAccessor headerAccessor) {
        String token = extractTokenFromStompHeader(headerAccessor);

        String userId = extractUserIdFromToken(token);

        if (Objects.equals(userId, null)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return Long.parseLong(userId);
    }

    public String extractTokenFromStompHeader(StompHeaderAccessor headerAccessor) {
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        } else {
            return (String) headerAccessor.getSessionAttributes().get("token");
        }
    }

    public String extractUserIdFromToken(String token) {
        try {
            String[] splitToken = token.split("\\.");
            if (splitToken.length != 3) {
                return null;
            }

            String payload = new String(java.util.Base64.getDecoder().decode(splitToken[1]));

            if (payload.contains("\"sub\":\"")) {
                int startIndex = payload.indexOf("\"sub\":\"") + 7;
                int endIndex = payload.indexOf("\"", startIndex);
                return payload.substring(startIndex, endIndex);
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting userId from token", e);
            return null;
        }
    }
}