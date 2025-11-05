package com.lts5.user.controller;

import com.lts5.user.payload.request.auth.LoginRequest;
import com.lts5.user.payload.request.auth.RegisterRequest;
import com.lts5.user.payload.request.auth.ResetPasswordRequest;
import com.lts5.user.payload.request.auth.RefreshTokenRequest;
import com.lts5.user.payload.response.LoginResponse;
import com.lts5.user.payload.response.TokenRefreshResponse;
import com.lts5.user.payload.response.WebLoginResponse;
import com.lts5.user.service.AuthService;
import com.primes.library.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "로그인, 회원가입 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public CommonResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.toDto());
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(loginRequest, response);

        // 모바일 환경에 대한 확장성을 고려하여, Cookie 방식이 아닌, Http Authorization Header 로 인증 처리
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + loginResponse.getAccessToken())
                .body(CommonResponse.createSuccess(loginResponse));
    }

    @Operation(summary = "쿠키 로그인 (웹 브라우저용)")
    @PostMapping("/login/web")
    public ResponseEntity<?> loginWithCookie(@RequestBody LoginRequest loginRequest, HttpServletResponse response, HttpServletRequest request) {
        log.info("쿠키 로그인 API 호출 - username: {}, tenantId: {}, remoteAddr: {}, userAgent: {}", 
                loginRequest.getUsername(), loginRequest.getTenantId(), 
                request.getRemoteAddr(), request.getHeader("User-Agent"));
        
        try {
            WebLoginResponse loginResponse = authService.loginWithCookie(loginRequest, response, request);

            // 쿠키에 토큰이 설정되므로 응답 본문에는 사용자 정보만 포함
            log.info("쿠키 로그인 API 성공 - username: {}, tenantId: {}", loginRequest.getUsername(), loginRequest.getTenantId());
            return ResponseEntity.ok()
                    .body(CommonResponse.createSuccess(loginResponse));
        } catch (Exception e) {
            log.error("쿠키 로그인 API 실패 - username: {}, tenantId: {}, error: {}", 
                    loginRequest.getUsername(), loginRequest.getTenantId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "ID 중복 조회 API (중복이면 true, 중복 아니면 false)")
    @GetMapping("/check-username")
    public CommonResponse<?> checkUsername(
            @Parameter(name = "username", description = "사용자 ID", example = "dong")
            @RequestParam String username,
            @Parameter(name = "tenantId", description = "테넌트 ID", example = "1")
            @RequestParam Short tenantId) {
        return CommonResponse.createSuccess(authService.checkUsername(username, tenantId));
    }

    @Operation(summary = "비밀번호 재설정 API")
    @PostMapping("/reset-password")
    public CommonResponse<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getNewPassword(), request.getTenantId());
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "Refresh Token을 사용한 Access Token 갱신")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse tokenRefreshResponse = authService.refreshToken(request.getRefreshToken());

        // 모바일 환경에 대한 확장성을 고려하여, Cookie 방식이 아닌, Http Authorization Header 로 인증 처리
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokenRefreshResponse.getAccessToken())
                .body(CommonResponse.createSuccess(tokenRefreshResponse));
    }

    @Operation(summary = "쿠키 토큰 갱신 (웹 브라우저용)")
    @PostMapping("/refresh/web")
    public ResponseEntity<?> refreshTokenWithCookie(HttpServletRequest request, HttpServletResponse response) {
        TokenRefreshResponse tokenRefreshResponse = authService.refreshTokenWithCookie(request, response);

        // 쿠키에 새로운 access token이 설정되므로 응답 본문에는 토큰 정보만 포함
        return ResponseEntity.ok()
                .body(CommonResponse.createSuccess(tokenRefreshResponse));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public CommonResponse<?> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "쿠키 로그아웃 (웹 브라우저용)")
    @PostMapping("/logout/web")
    public CommonResponse<?> logoutWithCookie(HttpServletRequest request, HttpServletResponse response) {
        authService.logoutWithCookie(request, response);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "권한 확인")
    @PostMapping("/check")
    public CommonResponse<?> check(
        @Parameter(description = "사용자 ID", example = "151967158571009") @RequestParam String userId,
        @Parameter(description = "권한 코드", example = "purchase:create") @RequestParam String code) {
        return CommonResponse.createSuccess(authService.checkPermission(userId, code));
    }
}
