package com.lts5.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lts5.user.entity.User;
import com.lts5.user.repository.user.UserRepository;
import com.primes.library.filter.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

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
class UserControllerTenantTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void 테넌트분리검증_다른테넌트사용자는조회되지않아야함() throws Exception {
        // Given - 테넌트 10001로 설정
        TenantContext.setTenantId((short) 10001);
        
        // 테넌트 10001의 사용자들 생성
        User tenant1User1 = User.builder()
                .username("tenant1user1")
                .password(passwordEncoder.encode("password123"))
                .name("테넌트1 사용자1")
                .email("tenant1user1@example.com")
                .isTenantAdmin("0")
                .isDelete(false)
                .isUse(true)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        User tenant1User2 = User.builder()
                .username("tenant1user2")
                .password(passwordEncoder.encode("password123"))
                .name("테넌트1 사용자2")
                .email("tenant1user2@example.com")
                .isTenantAdmin("0")
                .isDelete(false)
                .isUse(true)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(tenant1User1);
        userRepository.save(tenant1User2);
        userRepository.flush();
        
        // 테넌트를 10002로 변경
        TenantContext.setTenantId((short) 10002);
        
        // 테넌트 10002의 사용자 생성
        User tenant2User = User.builder()
                .username("tenant2user")
                .password(passwordEncoder.encode("password123"))
                .name("테넌트2 사용자")
                .email("tenant2user@example.com")
                .isTenantAdmin("0")
                .isDelete(false)
                .isUse(true)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(tenant2User);
        userRepository.flush();
        
        // 디버깅: 저장된 데이터 확인
        System.out.println("=== 저장된 사용자 데이터 ===");
        System.out.println("전체 사용자 목록: " + userRepository.findAll());
        System.out.println("현재 TenantContext: " + TenantContext.getTenantId());
        System.out.println("================================");

        // When & Then - 테넌트 10002로 조회 시 해당 테넌트 사용자만 조회되어야 함
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("tenant2user"))
                .andExpect(jsonPath("$.data.content[0].name").value("테넌트2 사용자"));
    }

    @Test
    void 테넌트분리검증_테넌트변경시조회결과변경확인() throws Exception {
        // Given - 각 테넌트별로 사용자 생성
        
        // 테넌트 10001로 설정하고 사용자 생성
        TenantContext.setTenantId((short) 10001);
        
        User tenant1User = User.builder()
                .username("tenant1user")
                .password(passwordEncoder.encode("password123"))
                .name("테넌트1 사용자")
                .email("tenant1user@example.com")
                .isTenantAdmin("0")
                .isDelete(false)
                .isUse(true)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(tenant1User);
        userRepository.flush();
        
        // 테넌트 10002로 변경하고 사용자 생성
        TenantContext.setTenantId((short) 10002);
        
        User tenant2User = User.builder()
                .username("tenant2user")
                .password(passwordEncoder.encode("password123"))
                .name("테넌트2 사용자")
                .email("tenant2user@example.com")
                .isTenantAdmin("0")
                .isDelete(false)
                .isUse(true)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(tenant2User);
        userRepository.flush();

        // When & Then 1 - 테넌트 10001로 조회
        TenantContext.setTenantId((short) 10001);
        
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("tenant1user"));

        // When & Then 2 - 테넌트 10002로 조회
        TenantContext.setTenantId((short) 10002);
        
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("tenant2user"));
    }

    @Test
    void 테넌트분리검증_실제조회결과확인() throws Exception {
        // Given - 여러 테넌트의 사용자들을 생성
        
        // 테넌트 10001 사용자들
        TenantContext.setTenantId((short) 10001);
        
        for (int i = 1; i <= 3; i++) {
            User user = User.builder()
                    .username("tenant1user" + i)
                    .password(passwordEncoder.encode("password123"))
                    .name("테넌트1 사용자" + i)
                    .email("tenant1user" + i + "@example.com")
                    .isTenantAdmin("0")
                    .isDelete(false)
                    .isUse(true)
                    .createdAt(LocalDateTime.now())
                    .createdBy("testuser")
                    .updatedAt(LocalDateTime.now())
                    .updatedBy("testuser")
                    .build();
            userRepository.save(user);
        }
        
        // 테넌트 10002 사용자들
        TenantContext.setTenantId((short) 10002);
        
        for (int i = 1; i <= 2; i++) {
            User user = User.builder()
                    .username("tenant2user" + i)
                    .password(passwordEncoder.encode("password123"))
                    .name("테넌트2 사용자" + i)
                    .email("tenant2user" + i + "@example.com")
                    .isTenantAdmin("0")
                    .isDelete(false)
                    .isUse(true)
                    .createdAt(LocalDateTime.now())
                    .createdBy("testuser")
                    .updatedAt(LocalDateTime.now())
                    .updatedBy("testuser")
                    .build();
            userRepository.save(user);
        }
        
        userRepository.flush();
        
        // 디버깅 정보 출력
        System.out.println("=== 테넌트별 사용자 생성 완료 ===");
        System.out.println("테넌트 10001: 3명, 테넌트 10002: 2명");
        System.out.println("전체 사용자 수: " + userRepository.findAll().size());
        
        // When & Then 1 - 테넌트 10001로 조회 (3명이 조회되어야 함)
        TenantContext.setTenantId((short) 10001);
        System.out.println("현재 TenantContext: " + TenantContext.getTenantId());
        
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalElements").value(3));

        // When & Then 2 - 테넌트 10002로 조회 (2명이 조회되어야 함)
        TenantContext.setTenantId((short) 10002);
        System.out.println("현재 TenantContext: " + TenantContext.getTenantId());
        
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void 실제문제재현_테넌트20156으로조회시10001테넌트데이터도조회되는문제() throws Exception {
        // Given - 실제 문제 상황 재현
        
        // 테넌트 10001 사용자 생성
        TenantContext.setTenantId((short) 10001);
        
        User tenant10001User = User.builder()
                .username("user10001")
                .password(passwordEncoder.encode("password123"))
                .name("테넌트10001 사용자")
                .email("user10001@example.com")
                .isTenantAdmin("0")
                .isDelete(false)
                .isUse(true)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(tenant10001User);
        
        // 테넌트 20156 사용자 생성
        TenantContext.setTenantId((short) 20156);
        
        User tenant20156User = User.builder()
                .username("user20156")
                .password(passwordEncoder.encode("password123"))
                .name("테넌트20156 사용자")
                .email("user20156@example.com")
                .isTenantAdmin("0")
                .isDelete(false)
                .isUse(true)
                .createdAt(LocalDateTime.now())
                .createdBy("testuser")
                .updatedAt(LocalDateTime.now())
                .updatedBy("testuser")
                .build();
        
        userRepository.save(tenant20156User);
        userRepository.flush();
        
        // 디버깅 정보
        System.out.println("=== 실제 문제 재현 테스트 ===");
        System.out.println("테넌트 10001 사용자: " + tenant10001User.getUsername());
        System.out.println("테넌트 20156 사용자: " + tenant20156User.getUsername());
        System.out.println("전체 사용자 수: " + userRepository.findAll().size());
        
        // When & Then - 테넌트 20156으로 조회 시 해당 테넌트 사용자만 조회되어야 함
        TenantContext.setTenantId((short) 20156);
        System.out.println("현재 TenantContext: " + TenantContext.getTenantId());
        
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                // 만약 테넌트 분리가 제대로 안 되면 2개가 조회될 것임
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("user20156"));
        
        // 추가 검증: 조회된 사용자들의 테넌트ID 확인
        System.out.println("=== 조회 결과 분석 ===");
        // 실제로는 여기서 문제가 발생할 수 있음
    }
}