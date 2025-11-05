package com.lts5.user.controller;

import com.lts5.user.service.RedisHealthService;
import com.lts5.user.service.DatabaseTokenStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;
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
class HealthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private RedisHealthService redisHealthService;

    @MockBean
    private DatabaseTokenStorageService databaseTokenStorageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void Redis연결상태확인_정상상태_성공응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(true);
        when(redisHealthService.testConnection()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/health/redis"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.redisHealthy").value(true))
                .andExpect(jsonPath("$.data.connectionTest").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).isRedisHealthy();
        verify(redisHealthService).testConnection();
    }

    @Test
    void Redis연결상태확인_연결실패상태_DOWN응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(false);
        when(redisHealthService.testConnection()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/health/redis"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.redisHealthy").value(false))
                .andExpect(jsonPath("$.data.connectionTest").value(false))
                .andExpect(jsonPath("$.data.status").value("DOWN"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).isRedisHealthy();
        verify(redisHealthService).testConnection();
    }

    @Test
    void Redis재연결시도_재연결성공_성공응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(true);
        doNothing().when(redisHealthService).attemptReconnection();

        // When & Then
        mockMvc.perform(get("/health/redis/reconnect"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.reconnectionAttempted").value(true))
                .andExpect(jsonPath("$.data.redisHealthy").value(true))
                .andExpect(jsonPath("$.data.status").value("RECONNECTED"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).attemptReconnection();
        verify(redisHealthService).isRedisHealthy();
    }

    @Test
    void Redis재연결시도_재연결실패_실패응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(false);
        doNothing().when(redisHealthService).attemptReconnection();

        // When & Then
        mockMvc.perform(get("/health/redis/reconnect"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.reconnectionAttempted").value(true))
                .andExpect(jsonPath("$.data.redisHealthy").value(false))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).attemptReconnection();
        verify(redisHealthService).isRedisHealthy();
    }

    @Test
    void 토큰저장소상태확인_모든서비스정상_UP응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(true);
        when(redisHealthService.testConnection()).thenReturn(true);
        when(databaseTokenStorageService.existsRefreshToken("health_check_test")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/health/token-storage"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.redis.healthy").value(true))
                .andExpect(jsonPath("$.data.redis.connectionTest").value(true))
                .andExpect(jsonPath("$.data.redis.status").value("UP"))
                .andExpect(jsonPath("$.data.database.healthy").value(true))
                .andExpect(jsonPath("$.data.database.status").value("UP"))
                .andExpect(jsonPath("$.data.overallStatus").value("UP"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).isRedisHealthy();
        verify(redisHealthService).testConnection();
        verify(databaseTokenStorageService).existsRefreshToken("health_check_test");
    }

    @Test
    void 토큰저장소상태확인_Redis실패DB정상_UP응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(false);
        when(redisHealthService.testConnection()).thenReturn(false);
        when(databaseTokenStorageService.existsRefreshToken("health_check_test")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/health/token-storage"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.redis.healthy").value(false))
                .andExpect(jsonPath("$.data.redis.connectionTest").value(false))
                .andExpect(jsonPath("$.data.redis.status").value("DOWN"))
                .andExpect(jsonPath("$.data.database.healthy").value(true))
                .andExpect(jsonPath("$.data.database.status").value("UP"))
                .andExpect(jsonPath("$.data.overallStatus").value("UP"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).isRedisHealthy();
        verify(redisHealthService).testConnection();
        verify(databaseTokenStorageService).existsRefreshToken("health_check_test");
    }

    @Test
    void 토큰저장소상태확인_Redis정상DB실패_UP응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(true);
        when(redisHealthService.testConnection()).thenReturn(true);
        when(databaseTokenStorageService.existsRefreshToken("health_check_test"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/health/token-storage"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.redis.healthy").value(true))
                .andExpect(jsonPath("$.data.redis.connectionTest").value(true))
                .andExpect(jsonPath("$.data.redis.status").value("UP"))
                .andExpect(jsonPath("$.data.database.healthy").value(false))
                .andExpect(jsonPath("$.data.database.status").value("DOWN"))
                .andExpect(jsonPath("$.data.overallStatus").value("UP"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).isRedisHealthy();
        verify(redisHealthService).testConnection();
        verify(databaseTokenStorageService).existsRefreshToken("health_check_test");
    }

    @Test
    void 토큰저장소상태확인_모든서비스실패_DOWN응답반환() throws Exception {
        // Given
        when(redisHealthService.isRedisHealthy()).thenReturn(false);
        when(redisHealthService.testConnection()).thenReturn(false);
        when(databaseTokenStorageService.existsRefreshToken("health_check_test"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/health/token-storage"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.redis.healthy").value(false))
                .andExpect(jsonPath("$.data.redis.connectionTest").value(false))
                .andExpect(jsonPath("$.data.redis.status").value("DOWN"))
                .andExpect(jsonPath("$.data.database.healthy").value(false))
                .andExpect(jsonPath("$.data.database.status").value("DOWN"))
                .andExpect(jsonPath("$.data.overallStatus").value("DOWN"))
                .andExpect(jsonPath("$.data.timestamp").exists());

        verify(redisHealthService).isRedisHealthy();
        verify(redisHealthService).testConnection();
        verify(databaseTokenStorageService).existsRefreshToken("health_check_test");
    }
}
