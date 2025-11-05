package com.lts5.user.payload.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotNull
    @Schema(description = "사용자 계정", example = "dong")
    String username;

    @NotNull
    @Schema(description = "새 비밀번호", example = "dong")
    String newPassword;

    @NotNull
    @Schema(description = "테넌트 ID", example = "10001")
    Short tenantId;
}
