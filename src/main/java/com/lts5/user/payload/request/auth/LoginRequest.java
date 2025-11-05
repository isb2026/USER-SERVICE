package com.lts5.user.payload.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class LoginRequest {
    @NotNull
    @Schema(description = "사용자 계정", example = "dong")
    private String username;

    @NotNull
    @Schema(description = "비밀번호", example = "dong")
    private String password;

    @NotNull
    @Schema(description = "테넌트 ID", example = "10001")
    private Short tenantId;
}
