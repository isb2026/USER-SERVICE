package com.lts5.user.controller;

import com.lts5.user.service.RedisHealthService;
import com.lts5.user.service.DatabaseTokenStorageService;
import com.primes.library.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "헬스체크 API")
public class HealthController {

    private final RedisHealthService redisHealthService;
    private final DatabaseTokenStorageService databaseTokenStorageService;

    @Operation(summary = "Redis 연결 상태 확인")
    @GetMapping("/redis")
    public CommonResponse<?> checkRedisHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        boolean isHealthy = redisHealthService.isRedisHealthy();
        boolean connectionTest = redisHealthService.testConnection();
        
        healthInfo.put("redisHealthy", isHealthy);
        healthInfo.put("connectionTest", connectionTest);
        healthInfo.put("status", connectionTest ? "UP" : "DOWN");
        healthInfo.put("timestamp", System.currentTimeMillis());
        
        return CommonResponse.createSuccess(healthInfo);
    }

    @Operation(summary = "Redis 재연결 시도")
    @GetMapping("/redis/reconnect")
    public CommonResponse<?> attemptRedisReconnection() {
        Map<String, Object> result = new HashMap<>();
        
        redisHealthService.attemptReconnection();
        boolean isHealthy = redisHealthService.isRedisHealthy();
        
        result.put("reconnectionAttempted", true);
        result.put("redisHealthy", isHealthy);
        result.put("status", isHealthy ? "RECONNECTED" : "FAILED");
        result.put("timestamp", System.currentTimeMillis());
        
        return CommonResponse.createSuccess(result);
    }

    @Operation(summary = "토큰 저장소 상태 확인")
    @GetMapping("/token-storage")
    public CommonResponse<?> checkTokenStorageHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        // Redis 상태 확인
        boolean redisHealthy = redisHealthService.isRedisHealthy();
        boolean redisConnectionTest = redisHealthService.testConnection();
        
        // DB 상태 확인 (간단한 테스트)
        boolean dbHealthy = true;
        try {
            databaseTokenStorageService.existsRefreshToken("health_check_test");
        } catch (Exception e) {
            dbHealthy = false;
        }
        
        healthInfo.put("redis", Map.of(
            "healthy", redisHealthy,
            "connectionTest", redisConnectionTest,
            "status", redisConnectionTest ? "UP" : "DOWN"
        ));
        
        healthInfo.put("database", Map.of(
            "healthy", dbHealthy,
            "status", dbHealthy ? "UP" : "DOWN"
        ));
        
        healthInfo.put("overallStatus", (redisHealthy || dbHealthy) ? "UP" : "DOWN");
        healthInfo.put("timestamp", System.currentTimeMillis());
        
        return CommonResponse.createSuccess(healthInfo);
    }
} 