package com.mts.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.application.security.AuthController;
import com.mts.application.security.TokenService;
import com.mts.application.security.payload.AuthDtos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(TestAuthExceptionHandler.class)
@TestPropertySource(properties = {
        "security.jwt.issuer=mts-app",
        "security.jwt.expiry-minutes=30"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200 and token")
    void login_success() throws Exception {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("api-user", "Passw0rd!");
        Authentication auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                .authenticated("api-user", null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(tokenService.generateToken(any(), any(), any(), any()))
                .thenReturn("mock-jwt");


        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("mock-jwt").header("alg", "HS256")
                        .claim("sub", "api-user").claim("scope", "transfers.read transfers.write")
                        .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(1800)).build());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("mock-jwt"))
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid credentials returns 401")
    void login_invalidCredentials_returns401() throws Exception {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("api-user", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
