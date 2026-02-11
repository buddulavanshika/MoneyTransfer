package com.mts.application.security;

import com.mts.application.entities.Account;
import com.mts.application.repository.AccountRepository;
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
    private final AccountRepository accountRepository;
    private final long expiryMinutes;
    private final String issuer;

    public AuthController(AuthenticationManager authManager,
                          TokenService tokenService,
                          AccountRepository accountRepository,
                          @Value("${security.jwt.expiry-minutes}") long expiryMinutes,
                          @Value("${security.jwt.issuer}") String issuer) {
        this.authManager = authManager;
        this.tokenService = tokenService;
        this.accountRepository = accountRepository;
        this.expiryMinutes = expiryMinutes;
        this.issuer = issuer;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
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

            // Find account by holder name (username matches holder name)
            Account account = accountRepository.findByHolderName(req.username())
                    .orElseThrow(() -> new RuntimeException("Account not found for user: " + req.username()));

            return ResponseEntity.ok(new LoginResponse(token, account.getId(), account.getHolderName()));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("BAD_CREDENTIALS", "Invalid username or password"));
        } catch (DisabledException ex) {
            return ResponseEntity.status(403)
                    .body(new ErrorResponse("USER_DISABLED", "User account is disabled"));
        } catch (LockedException ex) {
            return ResponseEntity.status(423)
                    .body(new ErrorResponse("USER_LOCKED", "User account is locked"));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("INVALID_REQUEST", ex.getMessage()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("AUTHENTICATION_FAILED", "Authentication failed"));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
        }
    }

    // Error response for login failures
    private record ErrorResponse(String code, String message) {}
}