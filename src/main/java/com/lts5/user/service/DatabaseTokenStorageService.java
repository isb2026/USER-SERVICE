package com.lts5.user.service;

import com.lts5.user.entity.RefreshToken;
import com.lts5.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DatabaseTokenStorageService implements TokenStorageService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void saveRefreshToken(String username, String refreshToken, long expirationTime) {
        try {
            // 기존 토큰들을 모두 삭제 (한 사용자당 하나의 토큰만 유지)
            refreshTokenRepository.deleteByUsername(username);
            
            // 새로운 토큰 저장
            LocalDateTime expiresAt = LocalDateTime.now().plusNanos(expirationTime * 1_000_000);
            RefreshToken token = RefreshToken.builder()
                    .username(username)
                    .refreshToken(refreshToken)
                    .expiresAt(expiresAt)
                    .build();
            
            refreshTokenRepository.save(token);
            log.debug("Refresh token saved to database for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to save refresh token to database for user: {}", username, e);
            throw e;
        }
    }

    @Override
    public String getRefreshToken(String username) {
        try {
            LocalDateTime now = LocalDateTime.now();
            return refreshTokenRepository.findValidTokenByUsername(username, now)
                    .map(RefreshToken::getRefreshToken)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to get refresh token from database for user: {}", username, e);
            throw e;
        }
    }

    @Override
    public void deleteRefreshToken(String username) {
        try {
            refreshTokenRepository.deleteByUsername(username);
            log.debug("Refresh token deleted from database for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to delete refresh token from database for user: {}", username, e);
            throw e;
        }
    }

    @Override
    public boolean existsRefreshToken(String username) {
        try {
            LocalDateTime now = LocalDateTime.now();
            return refreshTokenRepository.existsValidTokenByUsername(username, now);
        } catch (Exception e) {
            log.error("Failed to check refresh token existence in database for user: {}", username, e);
            throw e;
        }
    }

    /**
     * 만료된 토큰들을 정리합니다. (매일 새벽 2시에 실행)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = refreshTokenRepository.deleteExpiredTokens(now);
            if (deletedCount > 0) {
                log.info("만료된 refresh token {}개를 정리했습니다.", deletedCount);
            }
        } catch (Exception e) {
            log.error("만료된 토큰 정리 중 오류 발생", e);
        }
    }
} 