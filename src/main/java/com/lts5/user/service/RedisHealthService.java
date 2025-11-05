package com.lts5.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisHealthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AtomicBoolean isRedisHealthy = new AtomicBoolean(true);
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);

    /**
     * Redis 연결 상태를 확인합니다.
     * @return Redis가 정상적으로 연결되어 있으면 true
     */
    public boolean isRedisHealthy() {
        return isRedisHealthy.get();
    }

    /**
     * Redis 연결을 테스트합니다.
     * @return 연결 테스트 성공 여부
     */
    public boolean testConnection() {
        try {
            redisTemplate.opsForValue().get("health_check");
            if (!isRedisHealthy.get()) {
                log.info("✅ Redis 연결이 복구되었습니다.");
                isRedisHealthy.set(true);
            }
            return true;
        } catch (Exception e) {
            log.warn("❌ Redis 연결 테스트 실패: {}", e.getMessage());
            isRedisHealthy.set(false);
            return false;
        }
    }

    /**
     * 주기적으로 Redis 연결 상태를 확인합니다. (30초마다)
     */
    @Scheduled(fixedRate = 30000)
    public void healthCheck() {
        if (isReconnecting.get()) {
            log.debug("Redis 재연결 중... 스킵");
            return;
        }

        try {
            testConnection();
        } catch (Exception e) {
            log.error("Redis 헬스체크 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * Redis 재연결을 시도합니다.
     */
    public void attemptReconnection() {
        if (isReconnecting.compareAndSet(false, true)) {
            log.info("🔄 Redis 재연결을 시도합니다...");
            
            try {
                // 여러 번 재연결 시도
                for (int i = 0; i < 3; i++) {
                    if (testConnection()) {
                        log.info("✅ Redis 재연결 성공!");
                        break;
                    }
                    
                    if (i < 2) {
                        log.info("재연결 시도 {} 실패, 5초 후 재시도...", i + 1);
                        Thread.sleep(5000);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Redis 재연결 중 인터럽트 발생", e);
            } finally {
                isReconnecting.set(false);
            }
        }
    }

    /**
     * Redis 연결 상태를 로그로 출력합니다.
     */
    public void logConnectionStatus() {
        boolean healthy = testConnection();
        log.info("Redis 연결 상태: {}", healthy ? "✅ 정상" : "❌ 연결 끊김");
    }
} 