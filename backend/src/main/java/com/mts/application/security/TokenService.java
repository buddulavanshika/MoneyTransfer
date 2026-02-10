package com.mts.application.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class TokenService {

    private final SecretKeySpec key;
    private final String issuer;

    public TokenService(@Value("${security.jwt.secret}") String secretBase64,
                        @Value("${security.jwt.issuer}") String issuer) {
        this.issuer = issuer;

        // Sanitize secret: trim, strip quotes, remove whitespace
        if (secretBase64 == null) {
            throw new IllegalArgumentException("security.jwt.secret is not set");
        }
        String s = secretBase64.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        s = s.replaceAll("\\s", "");

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("security.jwt.secret is not valid Base64", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "security.jwt.secret must decode to >= 32 bytes for HS256; got " + keyBytes.length + " bytes"
            );
        }
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateToken(String subject, String scope, Instant issuedAt, Instant expiresAt) {
        try {
            JWSSigner signer = new MACSigner(key.getEncoded());

            // JOSE header using Nimbus (not Spring)
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .subject(subject)
                    .claim("scope", scope) // space-delimited scopes
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("TOKEN_SIGNING_FAILED", e);
        }
    }
}