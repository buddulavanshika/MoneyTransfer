package com.mts.application.security;

import com.mts.application.security.payload.AuthDtos.LoginRequest;
import com.mts.application.security.payload.AuthDtos.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final TokenService tokenService;
    private final long expiryMinutes;
    private final String issuer;

    public AuthController(AuthenticationManager authManager,
                          TokenService tokenService,
                          @Value("${security.jwt.expiry-minutes}") long expiryMinutes,
                          @Value("${security.jwt.issuer}") String issuer) {
        this.authManager = authManager;
        this.tokenService = tokenService;
        this.expiryMinutes = expiryMinutes;
        this.issuer = issuer;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );

            Instant now = Instant.now();
            long expirySeconds = Math.max(60, expiryMinutes * 60); // guard non-positive
            Instant exp = now.plusSeconds(expirySeconds);

            String scope = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("SCOPE_"))
                    .map(a -> a.substring("SCOPE_".length()))
                    .collect(Collectors.joining(" "));

            String token = tokenService.generateToken(auth.getName(), scope, now, exp);

            return ResponseEntity.ok(new LoginResponse("Bearer", token, expirySeconds));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new LoginResponse("error", "BAD_CREDENTIALS", 0));
        } catch (DisabledException ex) {
            return ResponseEntity.status(403).body(new LoginResponse("error", "USER_DISABLED", 0));
        } catch (LockedException ex) {
            return ResponseEntity.status(423).body(new LoginResponse("error", "USER_LOCKED", 0));
        } catch (IllegalArgumentException ex) {
            // from secret validation
            ex.printStackTrace();
            return ResponseEntity.status(400).body(new LoginResponse("error", ex.getMessage(), 0));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(new LoginResponse("error", "AUTHENTICATION_FAILED", 0));
        } catch (RuntimeException ex) {
            // TOKEN_SIGNING_FAILED
            ex.printStackTrace();
            return ResponseEntity.status(500).body(new LoginResponse("error", "TOKEN_GENERATION_FAILED", 0));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(new LoginResponse("error", "TOKEN_GENERATION_FAILED", 0));
        }
    }
}