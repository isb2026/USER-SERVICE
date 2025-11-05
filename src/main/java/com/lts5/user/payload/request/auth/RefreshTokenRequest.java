package com.lts5.user.payload.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotNull
    @Schema(description = "Refresh Token", example = "tokenstring")
    private String refreshToken;
} 