package com.lts5.user.repository;

import com.lts5.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 사용자명으로 유효한 refresh token을 조회합니다.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.username = :username AND rt.isRevoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidTokenByUsername(@Param("username") String username, @Param("now") LocalDateTime now);

    /**
     * 사용자명으로 모든 refresh token을 조회합니다.
     */
    List<RefreshToken> findByUsername(String username);

    /**
     * 만료된 토큰들을 삭제합니다.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 사용자의 모든 refresh token을 삭제합니다.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.username = :username")
    void deleteByUsername(@Param("username") String username);

    /**
     * 사용자명으로 refresh token이 존재하는지 확인합니다.
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.username = :username AND rt.isRevoked = false AND rt.expiresAt > :now")
    boolean existsValidTokenByUsername(@Param("username") String username, @Param("now") LocalDateTime now);
} 