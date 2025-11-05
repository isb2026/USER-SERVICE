package com.lts5.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lts5.user.entity.Group;
import com.lts5.user.entity.Role;
import com.lts5.user.payload.request.group.GroupCreateRequest;
import com.lts5.user.payload.request.group.GroupUpdateAllRequest;
import com.lts5.user.payload.request.group.GroupUpdateRequest;
import com.lts5.user.repository.group.GroupRepository;
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
class GroupControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoleRepository roleRepository;

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
    void 그룹조회_유효한검색조건_페이지결과반환() throws Exception {
        // Given
        String groupName = "테스트그룹";
        
        // 테스트 그룹 생성
        Group group = Group.builder()
                .name(groupName)
                .description("테스트 그룹 설명")
                .isDelete(false)
                .build();
        groupRepository.save(group);
        groupRepository.flush();

        // When & Then
        mockMvc.perform(get("/groups")
                        .param("name", groupName)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].name").value(groupName))
                .andExpect(jsonPath("$.data.content[0].description").value("테스트 그룹 설명"));
    }

    @Test
    void 그룹생성_유효한생성데이터_생성된그룹정보반환() throws Exception {
        // Given
        GroupCreateRequest request1 = new GroupCreateRequest();
        request1.setName("개발팀");
        request1.setDescription("개발팀 그룹");

        GroupCreateRequest request2 = new GroupCreateRequest();
        request2.setName("디자인팀");
        request2.setDescription("디자인팀 그룹");

        List<GroupCreateRequest> requests = Arrays.asList(request1, request2);

        // When & Then
        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("개발팀"))
                .andExpect(jsonPath("$.data[0].description").value("개발팀 그룹"))
                .andExpect(jsonPath("$.data[1].name").value("디자인팀"))
                .andExpect(jsonPath("$.data[1].description").value("디자인팀 그룹"));
    }

    @Test
    void 그룹수정_유효한수정데이터_수정된그룹정보반환() throws Exception {
        // Given
        String originalName = "원본그룹";
        String updatedName = "수정된그룹";
        
        // 테스트 그룹 생성
        Group group = Group.builder()
                .name(originalName)
                .description("원본 설명")
                .isDelete(false)
                .build();
        Group savedGroup = groupRepository.save(group);
        groupRepository.flush();

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setName(updatedName);
        request.setDescription("수정된 설명");

        // When & Then
        mockMvc.perform(put("/groups/{id}", savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value(updatedName))
                .andExpect(jsonPath("$.data.description").value("수정된 설명"));
    }

    @Test
    void 그룹일괄수정_유효한수정데이터목록_수정된그룹목록반환() throws Exception {
        // Given
        Group group1 = Group.builder()
                .name("그룹1")
                .description("설명1")
                .isDelete(false)
                .build();
        Group group2 = Group.builder()
                .name("그룹2")
                .description("설명2")
                .isDelete(false)
                .build();
        
        Group savedGroup1 = groupRepository.save(group1);
        Group savedGroup2 = groupRepository.save(group2);
        groupRepository.flush();

        GroupUpdateAllRequest request1 = new GroupUpdateAllRequest();
        request1.setId(savedGroup1.getId());
        request1.setName("수정된그룹1");
        request1.setDescription("수정된설명1");

        GroupUpdateAllRequest request2 = new GroupUpdateAllRequest();
        request2.setId(savedGroup2.getId());
        request2.setName("수정된그룹2");
        request2.setDescription("수정된설명2");

        List<GroupUpdateAllRequest> requests = Arrays.asList(request1, request2);

        // When & Then
        mockMvc.perform(put("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("수정된그룹1"))
                .andExpect(jsonPath("$.data[1].name").value("수정된그룹2"));
    }

    @Test
    void 그룹삭제_존재하는그룹ID목록_성공응답반환() throws Exception {
        // Given
        Group group1 = Group.builder()
                .name("삭제할그룹1")
                .description("삭제할 그룹1 설명")
                .isDelete(false)
                .build();
        Group group2 = Group.builder()
                .name("삭제할그룹2")
                .description("삭제할 그룹2 설명")
                .isDelete(false)
                .build();
        
        Group savedGroup1 = groupRepository.save(group1);
        Group savedGroup2 = groupRepository.save(group2);
        groupRepository.flush();

        List<Long> ids = Arrays.asList(savedGroup1.getId(), savedGroup2.getId());

        // When & Then
        mockMvc.perform(delete("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 그룹필드값조회_유효한필드명_필드값목록반환() throws Exception {
        // Given
        Group group1 = Group.builder()
                .name("그룹A")
                .description("그룹A 설명")
                .isDelete(false)
                .build();
        Group group2 = Group.builder()
                .name("그룹B")
                .description("그룹B 설명")
                .isDelete(false)
                .build();
        
        groupRepository.save(group1);
        groupRepository.save(group2);
        groupRepository.flush();

        // When & Then
        mockMvc.perform(get("/groups/fields/name"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void 그룹에역할부여_유효한역할ID_그룹역할정보반환() throws Exception {
        // Given
        Group group = Group.builder()
                .name("테스트그룹")
                .description("테스트 그룹")
                .isDelete(false)
                .build();
        Group savedGroup = groupRepository.save(group);

        Role role = Role.builder()
                .tenantId((short) 10001)
                .name("테스트역할")
                .description("테스트 역할")
                .build();
        Role savedRole = roleRepository.save(role);
        
        groupRepository.flush();
        roleRepository.flush();

        List<Long> roleIds = Arrays.asList(savedRole.getId());
        // When & Then
        mockMvc.perform(post("/groups/{id}/roles", savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void 그룹에서역할해제_존재하는그룹과역할ID_성공응답반환() throws Exception {
        // Given
        Group group = Group.builder()
                .name("테스트그룹")
                .description("테스트 그룹")
                .isDelete(false)
                .build();
        Group savedGroup = groupRepository.save(group);

        Role role = Role.builder()
                .tenantId((short) 10001)
                .name("테스트역할")
                .description("테스트 역할")
                .build();
        Role savedRole = roleRepository.save(role);
        
        groupRepository.flush();
        roleRepository.flush();

        List<Long> roleIds = Arrays.asList(savedRole.getId());

        // 먼저 역할에 권한을 부여
        mockMvc.perform(post("/groups/{id}/roles", savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andDo(print())
                .andExpect(status().isOk());
        
        // When & Then
        mockMvc.perform(delete("/groups/{id}/roles", savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
} 