package com.lts5.user.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lts5.user.entity.Group;
import com.lts5.user.entity.Role;
import com.lts5.user.entity.User;
import com.lts5.user.payload.request.user.UserUpdateRequest;
import com.lts5.user.repository.group.GroupRepository;
import com.lts5.user.repository.role.RoleRepository;
import com.lts5.user.repository.user.UserRepository;
import com.primes.library.filter.TenantContext;
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
class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantContext tenantContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // 테스트용 TenantContext 설정
        TenantContext.setTenantId((short) 10001);
    }

    @Test
    void 사용자수정_유효한수정데이터_수정된사용자정보반환() throws Exception {
        // Given
        String username = "testuser";
        String originalName = "테스트 사용자";
        String updatedName = "수정된 사용자";
        
        // TenantContext 확인
        System.out.println("TenantContext.getTenantId(): " + TenantContext.getTenantId());
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .name(originalName)
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        userRepository.save(user);
        userRepository.flush();
        
        // 디버깅: 저장된 데이터 확인
        System.out.println("=== 저장된 사용자 데이터 ===");
        System.out.println("저장된 사용자: " + user);
        System.out.println("전체 사용자 목록: " + userRepository.findAll());
        System.out.println("특정 사용자 조회: " + userRepository.findByUsernameAndTenantIdAndIsDeleteFalse(username, (short) 10001));
        System.out.println("================================");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName(updatedName);
        request.setEmail("updated@example.com");
        request.setMobileTel("010-1234-5678");

        // When & Then
        mockMvc.perform(put("/{username}", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.name").value(updatedName))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.mobileTel").value("010-1234-5678"));
    }

    @Test
    void 사용자삭제_존재하는사용자명_성공응답반환() throws Exception {
        // Given
        String username = "testuser";
        
        // TenantContext 설정
        TenantContext.setTenantId((short) 10001);
        
        // 테스트 사용자 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자")
                .email("test@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        userRepository.save(user);
        userRepository.flush();

        // When & Then
        mockMvc.perform(delete("/{username}", username))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success")); 
    }

    @Test
    @Transactional
    void 사용자필드값조회_유효한필드명_필드값목록반환() throws Exception {
        // Given
        String username1 = "testuser1";
        String username2 = "testuser2";
        
        // TenantContext 설정
        TenantContext.setTenantId((short) 10001);
        
        // 테스트 사용자들 생성
        User user1 = User.builder()
                .username(username1)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자1")
                .email("test1@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        User user2 = User.builder()
                .username(username2)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자2")
                .email("test2@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.flush();

        // When & Then
        mockMvc.perform(get("/fields/username"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].value").value(username1))
                .andExpect(jsonPath("$.data[1].value").value(username2))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.hasSize(2)));
    }

    @Test
    @Transactional
    void 사용자필드값조회_유효한필드명_필터조건_필드값목록반환() throws Exception {
        // Given
        String username1 = "testuser1";
        String username2 = "testuser2";
        String username3 = "testuser3";
        
        // TenantContext 설정
        TenantContext.setTenantId((short) 10001);
        
        // 테스트 사용자들 생성
        User user1 = User.builder()     
                .username(username1)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자1")
                .email("test1@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("1")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        User user2 = User.builder()
                .username(username2)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자2")
                .email("test2@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();

        User user3 = User.builder()
                .username(username3)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자3")
                .email("test3@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.flush();

        // When & Then
        mockMvc.perform(get("/fields/username")
                        .param("isTenantAdmin", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data[0].value").value(username2))
                .andExpect(jsonPath("$.data[1].value").value(username3));
    }

    @Test
    void 사용자조회_검색조건없음_전체사용자목록반환() throws Exception {
        // Given
        String username1 = "testuser1";
        String username2 = "testuser2";
        
        // TenantContext 설정
        TenantContext.setTenantId((short) 10001);
        
        // 테스트 사용자들 생성
        User user1 = User.builder()
                .username(username1)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자1")
                .email("test1@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        User user2 = User.builder()
                .username(username2)
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자2")
                .email("test2@example.com")
                .tenantId((short) 10001)
                .isTenantAdmin("0")
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.flush();

        // When & Then
        mockMvc.perform(get(""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void 사용자에게그룹지정_유효한그룹ID_사용자그룹정보반환() throws Exception {
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
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        User savedUser = userRepository.save(user);

        // 테스트 그룹 생성
        Group group = Group.builder()
                .name("테스트그룹")
                .description("테스트 그룹")
                .isDelete(false)
                .build();
        Group savedGroup = groupRepository.save(group);
        
        userRepository.flush();
        groupRepository.flush();

        List<Long> groupIds = Arrays.asList(savedGroup.getId());

        // When & Then
        mockMvc.perform(post("/{id}/groups", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void 사용자에게서그룹해제_존재하는사용자와그룹ID_성공응답반환() throws Exception {
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
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        User savedUser = userRepository.save(user);

        // 테스트 그룹 생성
        Group group = Group.builder()
                .name("테스트그룹")
                .description("테스트 그룹")
                .isDelete(false)
                .build();
        Group savedGroup = groupRepository.save(group);
        
        userRepository.flush();
        groupRepository.flush();

        List<Long> groupIds = Arrays.asList(savedGroup.getId());

        // 먼저 사용자에게 그룹을 부여
        mockMvc.perform(post("/{id}/groups", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupIds)))
                .andDo(print())
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(delete("/{userId}/groups", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void 사용자에게직접역할지정_유효한역할ID_사용자역할정보반환() throws Exception {
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
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        User savedUser = userRepository.save(user);

        // 테스트 역할 생성
        Role role = Role.builder()
                .tenantId((short) 10001)
                .name("테스트역할")
                .description("테스트 역할")
                .build();
        Role savedRole = roleRepository.save(role);
        
        userRepository.flush();
        roleRepository.flush();

        List<Long> roleIds = Arrays.asList(savedRole.getId());

        // When & Then
        mockMvc.perform(post("/{id}/roles", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void 사용자에게서역할해제_존재하는사용자와역할ID_성공응답반환() throws Exception {
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
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        User savedUser = userRepository.save(user);

        // 테스트 역할 생성
        Role role = Role.builder()
                .tenantId((short) 10001)
                .name("테스트역할")
                .description("테스트 역할")
                .build();
        Role savedRole = roleRepository.save(role);
        
        userRepository.flush();
        roleRepository.flush();

        List<Long> roleIds = Arrays.asList(savedRole.getId());

        // 먼저 사용자에게 역할을 부여
        mockMvc.perform(post("/{id}/roles", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andDo(print())
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(delete("/{userId}/roles", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
} 