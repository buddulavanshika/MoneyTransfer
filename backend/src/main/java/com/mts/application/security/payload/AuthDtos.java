package com.mts.application.security.payload;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(
            String token,
            Long accountId,
            String holderName
    ) {}
}