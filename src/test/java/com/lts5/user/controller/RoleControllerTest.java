package com.lts5.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lts5.user.entity.Permission;
import com.lts5.user.entity.Role;
import com.lts5.user.payload.request.role.RoleCreateRequest;
import com.lts5.user.payload.request.role.RoleUpdateRequest;
import com.lts5.user.payload.request.role.RoleUpdateAllRequest;
import com.lts5.user.repository.permission.PermissionRepository;
import com.lts5.user.repository.role.RoleRepository;
import com.primes.library.filter.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

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
class RoleControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

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
    void 역할조회_유효한검색조건_페이지결과반환() throws Exception {
        // Given
        String roleName = "테스트역할";
        
        // 테스트 역할 생성
        Role role = Role.builder()
                .tenantId((short) 10001)
                .name(roleName)
                .description("테스트 역할 설명")
                .build();
        roleRepository.save(role);
        roleRepository.flush();

        // When & Then
        mockMvc.perform(get("/roles")
                        .param("name", roleName)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].name").value(roleName))
                .andExpect(jsonPath("$.data.content[0].description").value("테스트 역할 설명"));
    }

    @Test
    void 역할생성_유효한생성데이터_생성된역할정보반환() throws Exception {
        // Given
        RoleCreateRequest request1 = new RoleCreateRequest();
        request1.setName("ADMIN");
        request1.setDescription("관리자 역할");

        RoleCreateRequest request2 = new RoleCreateRequest();
        request2.setName("USER");
        request2.setDescription("일반 사용자 역할");

        List<RoleCreateRequest> requests = Arrays.asList(request1, request2);

        // When & Then
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("ADMIN"))
                .andExpect(jsonPath("$.data[0].description").value("관리자 역할"))
                .andExpect(jsonPath("$.data[0].tenantId").value(10001))
                .andExpect(jsonPath("$.data[1].name").value("USER"))
                .andExpect(jsonPath("$.data[1].description").value("일반 사용자 역할"))
                .andExpect(jsonPath("$.data[1].tenantId").value(10001));
    }

    @Test
    void 역할수정_유효한수정데이터_수정된역할정보반환() throws Exception {
        // Given
        String originalName = "원본역할";
        String updatedName = "수정된역할";
        
        // 테스트 역할 생성
        Role role = Role.builder()
                .tenantId((short) 10001)
                .name(originalName)
                .description("원본 설명")
                .build();
        Role savedRole = roleRepository.save(role);
        roleRepository.flush();

        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setName(updatedName);
        request.setDescription("수정된 설명");

        // When & Then
        mockMvc.perform(put("/roles/{id}", savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value(updatedName))
                .andExpect(jsonPath("$.data.description").value("수정된 설명"));
    }

    @Test
    void 역할일괄수정_유효한수정데이터목록_수정된역할목록반환() throws Exception {
        // Given
        Role role1 = Role.builder()
                .tenantId((short) 10001)
                .name("역할1")
                .description("설명1")
                .build();
        Role role2 = Role.builder()
                .tenantId((short) 10001)
                .name("역할2")
                .description("설명2")
                .build();
        
        Role savedRole1 = roleRepository.save(role1);
        Role savedRole2 = roleRepository.save(role2);
        roleRepository.flush();

        RoleUpdateAllRequest request1 = new RoleUpdateAllRequest();
        request1.setId(savedRole1.getId());
        request1.setName("수정된역할1");
        request1.setDescription("수정된설명1");

        RoleUpdateAllRequest request2 = new RoleUpdateAllRequest();
        request2.setId(savedRole2.getId());
        request2.setName("수정된역할2");
        request2.setDescription("수정된설명2");

        List<RoleUpdateAllRequest> requests = Arrays.asList(request1, request2);

        // When & Then
        mockMvc.perform(put("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("수정된역할1"))
                .andExpect(jsonPath("$.data[1].name").value("수정된역할2"));
    }

    @Test
    void 역할삭제_존재하는역할ID목록_성공응답반환() throws Exception {
        // Given
        Role role1 = Role.builder()
                .tenantId((short) 10001)
                .name("삭제할역할1")
                .description("삭제할 역할1 설명")
                .build();
        Role role2 = Role.builder()
                .tenantId((short) 10001)
                .name("삭제할역할2")
                .description("삭제할 역할2 설명")
                .build();
        
        Role savedRole1 = roleRepository.save(role1);
        Role savedRole2 = roleRepository.save(role2);
        roleRepository.flush();

        List<Long> ids = Arrays.asList(savedRole1.getId(), savedRole2.getId());

        // When & Then
        mockMvc.perform(delete("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 역할필드값조회_유효한필드명_필드값목록반환() throws Exception {
        // Given
        Role role1 = Role.builder()
                .tenantId((short) 10001)
                .name("역할A")
                .description("역할A 설명")
                .build();
        Role role2 = Role.builder()
                .tenantId((short) 10001)
                .name("역할B")
                .description("역할B 설명")
                .build();
        
        roleRepository.save(role1);
        roleRepository.save(role2);
        roleRepository.flush();

        // When & Then
        mockMvc.perform(get("/roles/fields/name"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void 역할에권한부여_유효한권한ID_역할권한정보반환() throws Exception {
        // Given
        Role role = Role.builder()
                .tenantId((short) 10001)
                .name("테스트역할")
                .description("테스트 역할")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder()
                .tenantId((short) 10001)
                .code("TEST_PERMISSION")
                .serviceName("테스트권한")
                .description("테스트 권한")
                .build();
        Permission savedPermission = permissionRepository.save(permission);
        
        roleRepository.flush();
        permissionRepository.flush();

        List<Long> permissionIds = Arrays.asList(savedPermission.getId());

        // When & Then
        mockMvc.perform(post("/roles/{id}/permissions", savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void 역할에서권한해제_존재하는역할과권한ID_성공응답반환() throws Exception {
        // Given
        Role role = Role.builder()
                .tenantId((short) 10001)
                .name("테스트역할")
                .description("테스트 역할")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder()
                .tenantId((short) 10001)
                .code("TEST_PERMISSION")
                .serviceName("테스트권한")
                .description("테스트 권한")
                .build();
        Permission savedPermission = permissionRepository.save(permission);
        
        roleRepository.flush();
        permissionRepository.flush();

        List<Long> permissionIds = Arrays.asList(savedPermission.getId());

        // 먼저 역할에 권한을 부여
        mockMvc.perform(post("/roles/{id}/permissions", savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andDo(print())
                .andExpect(status().isOk());

        // When & Then - 권한 해제
        mockMvc.perform(delete("/roles/{id}/permissions", savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
} 