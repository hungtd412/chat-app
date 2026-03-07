package com.hungtd.chatapp.configuration;

import com.hungtd.chatapp.enums.ErrorCode;
import com.hungtd.chatapp.service.auth.TokenBlacklistService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

            if (!signedJWT.verify(verifier)) {
                throw new JwtException(ErrorCode.INVALID_KEY.getMessage());
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            Instant expiresAt = claimsSet.getExpirationTime() != null
                    ? claimsSet.getExpirationTime().toInstant()
                    : null;

            if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
                throw new JwtException(ErrorCode.INVALID_KEY.getMessage());
            }

            if (tokenBlacklistService.isBlacklisted(token)) {
                throw new JwtException(ErrorCode.UNAUTHENTICATED.getMessage());
            }

            Instant issuedAt = claimsSet.getIssueTime() != null ? claimsSet.getIssueTime().toInstant() : null;

            return new Jwt(
                    token,
                    issuedAt,
                    expiresAt,
                    signedJWT.getHeader().toJSONObject(),
                    claimsSet.getClaims()
            );
        } catch (ParseException | JOSEException e) {
            throw new JwtException(ErrorCode.INVALID_KEY.getMessage(), e);
        }
    }
}
