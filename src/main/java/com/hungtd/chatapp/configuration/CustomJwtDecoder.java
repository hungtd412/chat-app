package com.hungtd.chatapp.configuration;

import com.hungtd.chatapp.dto.request.IntrospectRequest;
import com.hungtd.chatapp.dto.response.IntrospectResponse;
import com.hungtd.chatapp.service.auth.impl.AuthenticationServiceImpl;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Autowired
    private AuthenticationServiceImpl authenticationServiceImpl;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            IntrospectResponse result = authenticationServiceImpl.introspect(IntrospectRequest.builder()
                    .token(token)
                    .build());

            if (!result.isValid()) {
                throw new JwtException("INVALID_KEY");
            }
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");

        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        return nimbusJwtDecoder.decode(token);
    }
}
