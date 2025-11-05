package com.lts5.user.service;

import com.lts5.user.dto.UserDto;
import com.lts5.user.entity.GroupRole;
import com.lts5.user.entity.Permission;
import com.lts5.user.entity.RolePermission;
import com.lts5.user.entity.UserGroup;
import com.lts5.user.entity.User;
import com.lts5.user.entity.UserRole;
import com.lts5.user.payload.request.auth.LoginRequest;
import com.lts5.user.payload.response.LoginResponse;
import com.lts5.user.payload.response.TokenRefreshResponse;
import com.lts5.user.payload.response.WebLoginResponse;
import com.lts5.user.repository.user.UserRepository;
import com.lts5.user.repository.auth.AuthRepository;
import com.lts5.user.repository.relation.UserGroupRepository;
import com.lts5.user.repository.relation.UserRoleRepository;
import com.lts5.user.repository.relation.RolePermissionRepository;
import com.lts5.user.repository.relation.GroupRoleRepository;
import com.lts5.user.repository.permission.PermissionRepository;
import com.lts5.user.util.JwtUtil;
import com.primes.library.common.codes.ErrorCode;
import com.primes.library.common.exceptions.DuplicatedUserException;
import com.primes.library.common.exceptions.EntityNotFoundException;
import com.primes.library.common.exceptions.ForbiddenException;
import com.primes.library.common.exceptions.IllegalPasswordException;
import com.primes.library.common.exceptions.IllegalArgumentException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenStorageService tokenStorageService;
    private final JwtUtil jwtUtil;
    private final UserGroupRepository userGroupRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final GroupRoleRepository groupRoleRepository;
    private final PermissionRepository permissionRepository;

    public void register(UserDto dto) {
        // 사용자 조회 (테넌트 ID와 함께 중복 체크) - AuthRepository 사용
        if (authRepository.existsByUsernameAndTenantId(dto.getUsername(), dto.getTenantId())) {
            throw new DuplicatedUserException(ErrorCode.DUPLICATED_USER_ERROR, "이미 존재하는 사용자명입니다.");
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        User user = dto.toEntity();
        authRepository.save(user);
    }

    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        // 사용자 조회 - AuthRepository 사용
        User user = authRepository.findByUsernameAndTenantIdAndIsDeleteFalse(loginRequest.getUsername(), loginRequest.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalPasswordException(ErrorCode.ILLEGAL_PASSWORD_EXCEPTION, "틀린 비밀번호입니다.");
        }

        // 추가 정보로 Access Token 생성
        Map<String, Object> claims = Map.of(
                "tenant_id", user.getTenantId(),
                "username", user.getUsername(),
                "user_id", user.getId()
        );
        String accessToken = JwtUtil.generateAccessToken(user.getUsername(), claims);
        
        // Refresh Token 생성
        String refreshToken = JwtUtil.generateRefreshToken(user.getUsername());
        
        // Refresh Token을 저장소에 저장
        tokenStorageService.saveRefreshToken(user.getUsername(), refreshToken, JwtUtil.getRefreshTokenExpirationTime());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.getAccessTokenExpirationTime())
                .build();
    }

    public WebLoginResponse loginWithCookie(LoginRequest loginRequest, HttpServletResponse response, HttpServletRequest request) {
        log.info("쿠키 로그인 시도 - username: {}, tenantId: {}, remoteAddr: {}, userAgent: {}", 
                loginRequest.getUsername(), loginRequest.getTenantId(), 
                request.getRemoteAddr(), request.getHeader("User-Agent"));
        
        try {
            // 사용자 조회 - AuthRepository 사용
            User user = authRepository.findByUsernameAndTenantIdAndIsDeleteFalse(loginRequest.getUsername(), loginRequest.getTenantId())
                    .orElseThrow(() -> {
                        log.warn("사용자 조회 실패 - username: {}, tenantId: {}", loginRequest.getUsername(), loginRequest.getTenantId());
                        return new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다.");
                    });

            log.info("사용자 조회 성공 - userId: {}, username: {}, tenantId: {}", user.getId(), user.getUsername(), user.getTenantId());

            // 비밀번호 확인
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("비밀번호 불일치 - username: {}, tenantId: {}", loginRequest.getUsername(), loginRequest.getTenantId());
                throw new IllegalPasswordException(ErrorCode.ILLEGAL_PASSWORD_EXCEPTION, "틀린 비밀번호입니다.");
            }
            
            log.info("비밀번호 확인 성공 - username: {}", loginRequest.getUsername());

        // 추가 정보로 Access Token 생성
        Map<String, Object> claims = Map.of(
                "tenant_id", user.getTenantId(),
                "username", user.getUsername(),
                "user_id", user.getId()
        );
        String accessToken = JwtUtil.generateAccessToken(user.getUsername(), claims);
        
        // Refresh Token 생성
        String refreshToken = JwtUtil.generateRefreshToken(user.getUsername());
        
        // Refresh Token을 저장소에 저장
        tokenStorageService.saveRefreshToken(user.getUsername(), refreshToken, JwtUtil.getRefreshTokenExpirationTime());
        
        // Origin 헤더에서 호스트 추출하여 해당 도메인에 맞는 쿠키 설정
        String origin = request.getHeader("Origin");
        String host = request.getHeader("Host");
        
        // Origin이 없으면 Host 헤더 사용
        if (origin == null || origin.isEmpty()) {
            origin = "http://" + host;
        }
        
        // 도메인 추출
        String domain = extractDomainFromOrigin(origin);
        
        // 해당 도메인에 맞는 쿠키 설정
        setCookiesForDomain(response, accessToken, refreshToken, domain);
        
            // 웹 브라우저용 로그인은 쿠키에 토큰이 설정되므로 응답에는 사용자 정보만 포함
            WebLoginResponse loginResponse = WebLoginResponse.builder()
                    .tokenType("Bearer")
                    .expiresIn(JwtUtil.getAccessTokenExpirationTime())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .email(user.getEmail())
                    .tenantId(user.getTenantId())
                    .isTenantAdmin(user.getIsTenantAdmin())
                    .build();
            
            log.info("쿠키 로그인 성공 - userId: {}, username: {}, tenantId: {}", user.getId(), user.getUsername(), user.getTenantId());
            return loginResponse;
            
        } catch (Exception e) {
            log.error("쿠키 로그인 실패 - username: {}, tenantId: {}, error: {}", 
                    loginRequest.getUsername(), loginRequest.getTenantId(), e.getMessage(), e);
            throw e;
        }
    }

    public boolean checkUsername(String username, Short tenantId) {
        return authRepository.existsByUsernameAndTenantId(username, tenantId);
    }

    public void resetPassword(String username, String newPassword, Short tenantId) {
        // 사용자 조회 - AuthRepository 사용
        User user = authRepository.findByUsernameAndTenantIdAndIsDeleteFalse(username, tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_EXIST_USER_ERROR, "존재하지 않는 사용자입니다."));

        // 새 비밀번호 설정
        user.setNewPassword(passwordEncoder.encode(newPassword));
        authRepository.save(user);
    }

    public TokenRefreshResponse refreshToken(String refreshToken) {
        // Refresh token 검증
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN_ERROR, "Refresh token이 필요합니다.");
        }

        // Refresh token에서 username 추출
        String username = jwtUtil.extractUsername(refreshToken);
        
        // Refresh token이 만료되었는지 확인
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN_ERROR, "token_expired");
        }

        // 저장된 refresh token과 일치하는지 확인
        String storedRefreshToken = tokenStorageService.getRefreshToken(username);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN_ERROR, "유효하지 않은 refresh token입니다.");
        }

        // 사용자 정보 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));

        // 새로운 access token 생성
        Map<String, Object> claims = Map.of(
                "tenant_id", user.getTenantId(),
                "username", user.getUsername(),
                "user_id", user.getId()
        );
        String newAccessToken = JwtUtil.generateAccessToken(user.getUsername(), claims);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.getAccessTokenExpirationTime())
                .build();
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN_ERROR, "Refresh token이 필요합니다.");
        }

        // Refresh token에서 username 추출
        String username = jwtUtil.extractUsername(refreshToken);
        
        // 저장된 refresh token과 일치하는지 확인
        String storedRefreshToken = tokenStorageService.getRefreshToken(username);
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            // Refresh token 삭제
            tokenStorageService.deleteRefreshToken(username);
        }
    }

    public void logoutWithCookie(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refresh token 추출
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            // Refresh token 삭제
            String username = jwtUtil.extractUsername(refreshToken);
            String storedRefreshToken = tokenStorageService.getRefreshToken(username);
            if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
                tokenStorageService.deleteRefreshToken(username);
            }
        }
        
        // 쿠키 삭제 (모든 도메인에 대해)
        clearCookies(response);
    }
    
    /**
     * 모든 도메인의 쿠키 삭제
     */
    private void clearCookies(HttpServletResponse response) {
        // localhost용 쿠키 삭제
        Cookie localhostAccessCookie = new Cookie("access_token", null);
        localhostAccessCookie.setMaxAge(0);
        localhostAccessCookie.setPath("/");
        response.addCookie(localhostAccessCookie);
        
        Cookie localhostRefreshCookie = new Cookie("refresh_token", null);
        localhostRefreshCookie.setMaxAge(0);
        localhostRefreshCookie.setPath("/");
        response.addCookie(localhostRefreshCookie);
        
        // primes-cloud.co.kr용 쿠키 삭제 (점 제거)
        try {
            Cookie primesAccessCookie = new Cookie("access_token", null);
            primesAccessCookie.setMaxAge(0);
            primesAccessCookie.setPath("/");
            primesAccessCookie.setDomain("primes-cloud.co.kr");
            response.addCookie(primesAccessCookie);
            
            Cookie primesRefreshCookie = new Cookie("refresh_token", null);
            primesRefreshCookie.setMaxAge(0);
            primesRefreshCookie.setPath("/");
            primesRefreshCookie.setDomain("primes-cloud.co.kr");
            response.addCookie(primesRefreshCookie);
        } catch (Exception e) {
            // 도메인 설정 실패 시 무시
        }
        
        // orcamaas.com용 쿠키 삭제 (점 제거)
        try {
            Cookie orcamaasAccessCookie = new Cookie("access_token", null);
            orcamaasAccessCookie.setMaxAge(0);
            orcamaasAccessCookie.setPath("/");
            orcamaasAccessCookie.setDomain("orcamaas.com");
            response.addCookie(orcamaasAccessCookie);
            
            Cookie orcamaasRefreshCookie = new Cookie("refresh_token", null);
            orcamaasRefreshCookie.setMaxAge(0);
            orcamaasRefreshCookie.setPath("/");
            orcamaasRefreshCookie.setDomain("orcamaas.com");
            response.addCookie(orcamaasRefreshCookie);
        } catch (Exception e) {
            // 도메인 설정 실패 시 무시
        }
    }

    public TokenRefreshResponse refreshTokenWithCookie(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refresh token 추출
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN_ERROR, "Refresh token이 필요합니다.");
        }

        // Refresh token에서 username 추출
        String username = jwtUtil.extractUsername(refreshToken);
        
        // Refresh token이 만료되었는지 확인
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN_ERROR, "token_expired");
        }

        // 저장된 refresh token과 일치하는지 확인
        String storedRefreshToken = tokenStorageService.getRefreshToken(username);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN_ERROR, "유효하지 않은 refresh token입니다.");
        }

        // 사용자 정보 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));

        // 새로운 access token 생성
        Map<String, Object> claims = Map.of(
                "tenant_id", user.getTenantId(),
                "username", user.getUsername(),
                "user_id", user.getId()
        );
        String newAccessToken = JwtUtil.generateAccessToken(user.getUsername(), claims);

        // Origin 헤더에서 호스트 추출하여 해당 도메인에 맞는 쿠키 설정
        String origin = request.getHeader("Origin");
        String host = request.getHeader("Host");
        
        // Origin이 없으면 Host 헤더 사용
        if (origin == null || origin.isEmpty()) {
            origin = "http://" + host;
        }
        
        // 도메인 추출
        String domain = extractDomainFromOrigin(origin);
        
        // 새로운 access token을 쿠키에 설정
        setAccessTokenCookie(response, newAccessToken, domain);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.getAccessTokenExpirationTime())
                .build();
    }
    
    /**
     * Access Token만 쿠키에 설정
     */
    private void setAccessTokenCookie(HttpServletResponse response, String accessToken, String domain) {
        boolean isSecure = domain != null;
        
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(isSecure);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) JwtUtil.getAccessTokenExpirationTime());
        if (domain != null) {
            accessCookie.setDomain(domain);
        }
        
        // SameSite 설정
        if (isSecure) {
            response.addHeader("Set-Cookie", 
                String.format("access_token=%s; Path=/; Domain=%s; Max-Age=%d; HttpOnly; Secure; SameSite=None", 
                    accessToken, domain, (int) JwtUtil.getAccessTokenExpirationTime()));
        } else {
            response.addCookie(accessCookie);
        }
    }

    public boolean checkPermission(String userId, String code) {
        // 사용자 조회
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));
        
        // 사용자의 모든 역할 ID 수집
        List<Long> roleIds = new ArrayList<>();
        
        // 그룹을 통한 역할 수집
        List<UserGroup> userGroups = userGroupRepository.findAllByIdUserId(user.getId());
        for (UserGroup userGroup : userGroups) {
            List<GroupRole> groupRoles = groupRoleRepository.findAllByIdGroupId(userGroup.getId().getGroupId());
            for (GroupRole groupRole : groupRoles) {
                roleIds.add(groupRole.getId().getRoleId());
            }
        }
        
        // 직접 할당된 역할 수집
        List<UserRole> userRoles = userRoleRepository.findAllByIdUserId(user.getId());
        for (UserRole userRole : userRoles) {
            roleIds.add(userRole.getId().getRoleId());
        }
        
        // 역할이 없으면 권한 없음
        if (roleIds.isEmpty()) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_ERROR, "접근 권한이 없습니다.");
        }
        
        // 해당 역할들의 권한 중에서 요청된 코드와 일치하는 것이 있는지 확인
        for (Long roleId : roleIds) {
            List<RolePermission> rolePermissions = rolePermissionRepository.findAllByIdRoleId(roleId);
            for (RolePermission rolePermission : rolePermissions) {
                Permission permission = permissionRepository.findById(rolePermission.getId().getPermissionId())
                        .orElse(null); // 권한이 삭제된 경우 무시
                if (permission != null && code.equals(permission.getCode())) {
                    return true; // 권한이 있으면 true 반환
                }
            }
        }
        
        // 권한이 없으면 403 에러 발생
        throw new ForbiddenException(ErrorCode.FORBIDDEN_ERROR, "해당 기능에 대한 접근 권한이 없습니다.");
    }
    
    /**
     * Origin에서 도메인 추출
     * @param origin Origin 헤더 값 (예: http://localhost:3000, https://api.primes-cloud.co.kr)
     * @return 도메인 (예: localhost, .primes-cloud.co.kr, .orcamaas.com)
     */
    private String extractDomainFromOrigin(String origin) {
        try {
            java.net.URL url = new java.net.URL(origin);
            String host = url.getHost();
            
            // localhost인 경우
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                return null; // 도메인 설정 없음
            }
            
            // primes-cloud.co.kr 도메인인 경우
            if (host.endsWith("primes-cloud.co.kr")) {
                return "primes-cloud.co.kr";
            }
            
            // orcamaas.com 도메인인 경우
            if (host.endsWith("orcamaas.com")) {
                return "orcamaas.com";
            }
            
            // 기타 도메인은 그대로 사용
            return host;
        } catch (Exception e) {
            return null; // 파싱 실패 시 도메인 설정 없음
        }
    }
    
    /**
     * 특정 도메인에 맞는 쿠키 설정
     * @param response HttpServletResponse
     * @param accessToken Access Token
     * @param refreshToken Refresh Token
     * @param domain 도메인 (null이면 localhost용)
     */
    private void setCookiesForDomain(HttpServletResponse response, String accessToken, String refreshToken, String domain) {
        boolean isSecure = domain != null; // 도메인이 설정된 경우에만 Secure
        
        // Access Token 쿠키
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(isSecure);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) JwtUtil.getAccessTokenExpirationTime());
        if (domain != null) {
            accessCookie.setDomain(domain);
        }
        // SameSite 설정
        if (isSecure) {
            response.addHeader("Set-Cookie", 
                String.format("access_token=%s; Path=/; Domain=%s; Max-Age=%d; HttpOnly; Secure; SameSite=None", 
                    accessToken, domain, (int) JwtUtil.getAccessTokenExpirationTime()));
        } else {
            response.addCookie(accessCookie);
        }
        
        // Refresh Token 쿠키
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(isSecure);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) JwtUtil.getRefreshTokenExpirationTime());
        if (domain != null) {
            refreshCookie.setDomain(domain);
        }
        // SameSite 설정
        if (isSecure) {
            response.addHeader("Set-Cookie", 
                String.format("refresh_token=%s; Path=/; Domain=%s; Max-Age=%d; HttpOnly; Secure; SameSite=None", 
                    refreshToken, domain, (int) JwtUtil.getRefreshTokenExpirationTime()));
        } else {
            response.addCookie(refreshCookie);
        }
    }
}
