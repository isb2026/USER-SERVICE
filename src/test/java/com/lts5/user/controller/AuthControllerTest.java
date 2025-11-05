package com.lts5.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lts5.user.entity.User;
import com.lts5.user.entity.Role;
import com.lts5.user.entity.Permission;
import com.lts5.user.entity.UserRole;
import com.lts5.user.entity.RolePermission;
import com.lts5.user.entity.ids.UserRoleId;
import com.lts5.user.entity.ids.RolePermissionId;
import com.lts5.user.payload.request.auth.LoginRequest;
import com.lts5.user.payload.request.auth.RegisterRequest;
import com.lts5.user.payload.request.auth.RefreshTokenRequest;
import com.lts5.user.payload.request.auth.ResetPasswordRequest;
import com.lts5.user.repository.user.UserRepository;
import com.lts5.user.repository.role.RoleRepository;
import com.lts5.user.repository.permission.PermissionRepository;
import com.lts5.user.repository.relation.UserRoleRepository;
import com.lts5.user.repository.relation.RolePermissionRepository;
import com.lts5.user.service.TokenStorageService;
import com.lts5.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(
    properties = {
        "spring.kafka.autoStartup=false",
        "spring.kafka.consumer.auto-startup=false",
        "spring.kafka.producer.auto-startup=false",
        "kafka.enabled=false",
        "primes.library.kafka.enabled=false"
    }
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=",
    "spring.kafka.consumer.bootstrap-servers=",
    "spring.kafka.producer.bootstrap-servers="
})
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenStorageService tokenStorageService;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void 회원가입_유효한사용자데이터_성공응답반환() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setName("테스트 사용자");
        request.setEmail("test@example.com");
        request.setTenantId((short) 10001);
        request.setIsTenantAdmin("0");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").isEmpty());
    }

    @Test
    void 로그인_유효한인증정보_액세스토큰과리프레시토큰반환() throws Exception {
        // Given
        String username = "testuser";
        String password = "password123";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setTenantId((short) 10001);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").exists());
    }

    @Test
    void 사용자명중복확인_존재하는사용자명_True반환() throws Exception {
        // Given
        String username = "testuser";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/auth/check-username")
                        .param("username", username)
                        .param("tenantId", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void 비밀번호재설정_유효한사용자명과새비밀번호_성공응답반환() throws Exception {
        // Given
        String username = "testuser";
        String newPassword = "newpassword123";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("oldpassword"))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUsername(username);
        request.setNewPassword(newPassword);
        request.setTenantId((short) 10001);

        // When & Then
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 토큰갱신_유효한리프레시토큰_새액세스토큰반환() throws Exception {
        // Given
        String username = "testuser";
        String password = "password123";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(user);

        // Refresh token 생성 및 저장
        String refreshToken = JwtUtil.generateRefreshToken(username);
        tokenStorageService.saveRefreshToken(username, refreshToken, JwtUtil.getRefreshTokenExpirationTime());

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").exists());
    }

    @Test
    void 로그아웃_유효한리프레시토큰_성공응답반환() throws Exception {
        // Given
        String username = "testuser";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(user);

        // Refresh token 생성 및 저장
        String refreshToken = JwtUtil.generateRefreshToken(username);
        tokenStorageService.saveRefreshToken(username, refreshToken, JwtUtil.getRefreshTokenExpirationTime());

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 회원가입_중복된사용자명_에러응답반환() throws Exception {
        // Given
        String username = "dupuser"; // 12글자 이하로 설정
        
        // 기존 사용자 생성
        User existingUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .name("기존 사용자")
                .email("existing@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("password123");
        request.setName("새 사용자");
        request.setEmail("new@example.com");
        request.setTenantId((short) 10001);
        request.setIsTenantAdmin("0");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 로그인_존재하지않는사용자_에러응답반환() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistentuser");
        request.setPassword("password123");
        request.setTenantId((short) 10001);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 로그인_잘못된비밀번호_에러응답반환() throws Exception {
        // Given
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(correctPassword))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(wrongPassword);
        request.setTenantId((short) 10001);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 사용자명중복확인_존재하지않는사용자명_False반환() throws Exception {
        // Given
        String username = "nonexistentuser";

        // When & Then
        mockMvc.perform(get("/auth/check-username")
                        .param("username", username)
                        .param("tenantId", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void 비밀번호재설정_존재하지않는사용자_에러응답반환() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setUsername("nonexistentuser");
        request.setNewPassword("newpassword123");
        request.setTenantId((short) 10001);

        // When & Then
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 토큰갱신_만료된리프레시토큰_에러응답반환() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired.refresh.token");

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is5xxServerError()); // JWT 파싱 오류로 500 반환
    }

    @Test
    void 토큰갱신_존재하지않는리프레시토큰_에러응답반환() throws Exception {
        // Given
        String username = "testuser";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        userRepository.save(user);

        // 유효한 JWT 토큰이지만 저장소에 없는 리프레시 토큰
        String refreshToken = JwtUtil.generateRefreshToken(username);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // 401 반환
    }

    @Test
    void 로그아웃_존재하지않는리프레시토큰_에러응답반환() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("nonexistent.refresh.token");

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is5xxServerError()); // JWT 파싱 오류로 500 반환
    }

    @Test
    void 회원가입_유효성검증실패_에러응답반환() throws Exception {
        // Given - 필수 필드가 누락된 요청
        RegisterRequest request = new RegisterRequest();
        // username과 password만 설정하고 나머지는 누락

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 권한확인_유효한사용자와권한코드_True반환() throws Exception {
        // Given
        String username = "testuser";
        String permissionCode = "user:read";
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .build();
        User savedUser = userRepository.save(user);

        // 권한 생성
        Permission permission = Permission.builder()
                .code(permissionCode)
                .description("사용자 읽기 권한")
                .serviceName("user-service")
                .tenantId((short) 10001)
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        // 역할 생성
        Role role = Role.builder()
                .name("USER_ROLE")
                .description("일반 사용자 역할")
                .tenantId((short) 10001)
                .build();
        Role savedRole = roleRepository.save(role);

        // 사용자-역할 관계 생성 (flush를 통해 즉시 DB에 반영)
        UserRole userRole = UserRole.builder()
                .id(new UserRoleId(savedUser.getId(), savedRole.getId()))
                .user(savedUser) 
                .role(savedRole)
                .tenantId((short) 10001)
                .build();
        UserRole savedUserRole = userRoleRepository.saveAndFlush(userRole);

        // 역할-권한 관계 생성 (flush를 통해 즉시 DB에 반영)
        RolePermission rolePermission = RolePermission.builder()
                .id(new RolePermissionId(savedRole.getId(), savedPermission.getId()))
                .role(savedRole)
                .permission(savedPermission)
                .tenantId((short) 10001)
                .build();
        RolePermission savedRolePermission = rolePermissionRepository.saveAndFlush(rolePermission);

        // When & Then
        mockMvc.perform(post("/auth/check")
                        .param("userId", savedUser.getId().toString())
                        .param("code", permissionCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value("true"));
    }
}