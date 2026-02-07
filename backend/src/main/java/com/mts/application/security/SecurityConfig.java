package com.mts.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Simple in-memory user for testing.
     * Replace with a DB-backed UserDetailsService when you’re ready.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails apiUser = User.withUsername("api-user")
                .password(encoder.encode("Passw0rd!"))
                .roles("USER")
                // map to "scope" claim during login (AuthController) so these become SCOPE_ authorities at runtime
                .authorities("SCOPE_transfers.read", "SCOPE_transfers.write")
                .build();
        return new InMemoryUserDetailsManager(apiUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // strong hash
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Convert "scope" claim → SCOPE_* authorities
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        scopes.setAuthorityPrefix("SCOPE_");
        scopes.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(scopes);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // === Public endpoints ===
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Swagger & OpenAPI endpoints (permit all)
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/doc.html"
                        ).permitAll()

                        // === Secured APIs by scope ===
                        .requestMatchers(HttpMethod.POST, "/api/transfers").hasAuthority("SCOPE_transfers.write")
                        .requestMatchers(HttpMethod.GET, "/api/transactions/**").hasAuthority("SCOPE_transfers.read")

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                // Resource Server with JWT (Nimbus under the hood)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("""
                        {"code":"UNAUTHORIZED","message":"Bearer token missing or invalid"}
                    """);
                        })
                )
                .exceptionHandling(eh -> eh
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("""
                        {"code":"FORBIDDEN","message":"Insufficient permissions"}
                    """);
                        })
                );

        return http.build();
    }
}