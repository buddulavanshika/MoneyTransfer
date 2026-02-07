package com.mts.application.security;

import com.mts.application.security.payload.AuthDtos.LoginRequest;
import com.mts.application.security.payload.AuthDtos.LoginResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;
    private final long expiryMinutes;
    private final String issuer;

    public AuthController(AuthenticationManager authManager,
                          JwtEncoder jwtEncoder,
                          @Value("${security.jwt.expiry-minutes}") long expiryMinutes,
                          @Value("${security.jwt.issuer}") String issuer) {
        this.authManager = authManager;
        this.jwtEncoder = jwtEncoder;
        this.expiryMinutes = expiryMinutes;
        this.issuer = issuer;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiryMinutes * 60);

        // Build scope string from authorities that start with SCOPE_
        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("SCOPE_"))
                .map(a -> a.substring("SCOPE_".length()))
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(auth.getName())
                .claim("scope", scope)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return ResponseEntity.ok(new LoginResponse("Bearer", token, expiryMinutes * 60));
    }
}