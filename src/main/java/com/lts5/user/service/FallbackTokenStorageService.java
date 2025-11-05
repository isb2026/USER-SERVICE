package com.lts5.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@RequiredArgsConstructor
public class FallbackTokenStorageService implements TokenStorageService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisHealthService redisHealthService;
    private final DatabaseTokenStorageService databaseTokenStorageService;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Override
    public void saveRefreshToken(String username, String refreshToken, long expirationTime) {
        // 먼저 Redis에 저장 시도
        if (tryRedisSave(username, refreshToken, expirationTime)) {
            return;
        }
        
        // Redis 실패 시 DB에 저장
        log.info("Redis 저장 실패, DB에 저장합니다. username: {}", username);
        databaseTokenStorageService.saveRefreshToken(username, refreshToken, expirationTime);
    }

    @Override
    public String getRefreshToken(String username) {
        // 먼저 Redis에서 조회 시도
        String token = tryRedisGet(username);
        if (token != null) {
            return token;
        }
        
        // Redis에서 조회 실패 시 DB에서 조회
        log.info("Redis 조회 실패, DB에서 조회합니다. username: {}", username);
        return databaseTokenStorageService.getRefreshToken(username);
    }

    @Override
    public void deleteRefreshToken(String username) {
        // Redis와 DB 모두에서 삭제
        tryRedisDelete(username);
        databaseTokenStorageService.deleteRefreshToken(username);
    }

    @Override
    public boolean existsRefreshToken(String username) {
        // 먼저 Redis에서 확인 시도
        Boolean redisExists = tryRedisExists(username);
        if (redisExists != null && redisExists) {
            return true;
        }
        
        // Redis 확인 실패 시 DB에서 확인
        return databaseTokenStorageService.existsRefreshToken(username);
    }

    private boolean tryRedisSave(String username, String refreshToken, long expirationTime) {
        try {
            if (!redisHealthService.isRedisHealthy()) {
                return false;
            }
            
            String key = REFRESH_TOKEN_PREFIX + username;
            redisTemplate.opsForValue().set(key, refreshToken, java.time.Duration.ofMillis(expirationTime));
            log.debug("Refresh token saved to Redis for user: {}", username);
            return true;
        } catch (Exception e) {
            log.warn("Redis 저장 실패: {}", e.getMessage());
            redisHealthService.attemptReconnection();
            return false;
        }
    }

    private String tryRedisGet(String username) {
        try {
            if (!redisHealthService.isRedisHealthy()) {
                return null;
            }
            
            String key = REFRESH_TOKEN_PREFIX + username;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis 조회 실패: {}", e.getMessage());
            redisHealthService.attemptReconnection();
            return null;
        }
    }

    private void tryRedisDelete(String username) {
        try {
            if (!redisHealthService.isRedisHealthy()) {
                return;
            }
            
            String key = REFRESH_TOKEN_PREFIX + username;
            redisTemplate.delete(key);
            log.debug("Refresh token deleted from Redis for user: {}", username);
        } catch (Exception e) {
            log.warn("Redis 삭제 실패: {}", e.getMessage());
            redisHealthService.attemptReconnection();
        }
    }

    private Boolean tryRedisExists(String username) {
        try {
            if (!redisHealthService.isRedisHealthy()) {
                return null;
            }
            
            String key = REFRESH_TOKEN_PREFIX + username;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis 존재 확인 실패: {}", e.getMessage());
            redisHealthService.attemptReconnection();
            return null;
        }
    }
} 