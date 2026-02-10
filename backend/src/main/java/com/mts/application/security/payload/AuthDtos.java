package com.mts.application.security.payload;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String tokenType, String accessToken, long expiresIn) {}
}