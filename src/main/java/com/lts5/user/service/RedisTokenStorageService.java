package com.lts5.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class RedisTokenStorageService implements TokenStorageService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisHealthService redisHealthService;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    
    @Override
    public void saveRefreshToken(String username, String refreshToken, long expirationTime) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            Duration duration = Duration.ofMillis(expirationTime);
            redisTemplate.opsForValue().set(key, refreshToken, duration);
            log.debug("Refresh token saved to Redis for user: {}", username);
        } catch (Exception e) {
            log.warn("Redis 저장 실패, 재연결 시도 후 재시도: {}", e.getMessage());
            redisHealthService.attemptReconnection();
            
            // 재연결 후 다시 시도
            try {
                String key = REFRESH_TOKEN_PREFIX + username;
                Duration duration = Duration.ofMillis(expirationTime);
                redisTemplate.opsForValue().set(key, refreshToken, duration);
                log.info("재연결 후 Refresh token 저장 성공 for user: {}", username);
            } catch (Exception retryException) {
                log.error("재연결 후에도 Redis 저장 실패 for user: {}", username, retryException);
                throw retryException;
            }
        }
    }
    
    @Override
    public String getRefreshToken(String username) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis 조회 실패, 재연결 시도 후 재시도: {}", e.getMessage());
            redisHealthService.attemptReconnection();
            
            // 재연결 후 다시 시도
            try {
                String key = REFRESH_TOKEN_PREFIX + username;
                String token = redisTemplate.opsForValue().get(key);
                log.info("재연결 후 Refresh token 조회 성공 for user: {}", username);
                return token;
            } catch (Exception retryException) {
                log.error("재연결 후에도 Redis 조회 실패 for user: {}", username, retryException);
                throw retryException;
            }
        }
    }
    
    @Override
    public void deleteRefreshToken(String username) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            redisTemplate.delete(key);
            log.debug("Refresh token deleted from Redis for user: {}", username);
        } catch (Exception e) {
            log.warn("Redis 삭제 실패, 재연결 시도 후 재시도: {}", e.getMessage());
            redisHealthService.attemptReconnection();
            
            // 재연결 후 다시 시도
            try {
                String key = REFRESH_TOKEN_PREFIX + username;
                redisTemplate.delete(key);
                log.info("재연결 후 Refresh token 삭제 성공 for user: {}", username);
            } catch (Exception retryException) {
                log.error("재연결 후에도 Redis 삭제 실패 for user: {}", username, retryException);
                throw retryException;
            }
        }
    }
    
    @Override
    public boolean existsRefreshToken(String username) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis 존재 확인 실패, 재연결 시도 후 재시도: {}", e.getMessage());
            redisHealthService.attemptReconnection();
            
            // 재연결 후 다시 시도
            try {
                String key = REFRESH_TOKEN_PREFIX + username;
                boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
                log.info("재연결 후 Refresh token 존재 확인 성공 for user: {}", username);
                return exists;
            } catch (Exception retryException) {
                log.error("재연결 후에도 Redis 존재 확인 실패 for user: {}", username, retryException);
                throw retryException;
            }
        }
    }
} 