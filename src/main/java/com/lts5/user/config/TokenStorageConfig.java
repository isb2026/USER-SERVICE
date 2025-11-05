package com.lts5.user.config;

import com.lts5.user.service.DatabaseTokenStorageService;
import com.lts5.user.service.FallbackTokenStorageService;
import com.lts5.user.service.RedisHealthService;
import com.lts5.user.service.TokenStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Configuration
public class TokenStorageConfig {
    
    @Bean
    @Primary
    public TokenStorageService tokenStorageService(RedisTemplate<String, String> redisTemplate, 
                                                   RedisHealthService redisHealthService,
                                                   DatabaseTokenStorageService databaseTokenStorageService) {
        log.info("Redis + DB Fallback 토큰 저장소를 초기화합니다.");
        return new FallbackTokenStorageService(redisTemplate, redisHealthService, databaseTokenStorageService);
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.token-storage.type", havingValue = "database", matchIfMissing = false)
    public TokenStorageService databaseTokenStorageService(DatabaseTokenStorageService databaseTokenStorageService) {
        log.info("DB 전용 토큰 저장소를 초기화합니다.");
        return databaseTokenStorageService;
    }
} 