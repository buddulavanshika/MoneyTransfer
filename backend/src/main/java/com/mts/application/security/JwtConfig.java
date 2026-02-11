package com.mts.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${security.jwt.secret}")
    private String secretBase64;

    @Value("${security.jwt.issuer}")
    private String issuer;

    private SecretKeySpec hs256Key() {
        if (secretBase64 == null) {
            throw new IllegalArgumentException("security.jwt.secret is not set");
        }
        String s = secretBase64.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        s = s.replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(s);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret must decode to >= 32 bytes for HS256; got " + keyBytes.length + " bytes");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var key = hs256Key();
        var decoder = NimbusJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}