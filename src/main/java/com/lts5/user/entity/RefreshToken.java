package com.lts5.user.entity;

import com.primes.library.audit.KafkaAuditEntityListener;
import com.primes.library.entity.SnowflakeId;
import com.primes.library.entity.SnowflakeIdEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class, KafkaAuditEntityListener.class, SnowflakeIdEntityListener.class})
@Builder
@NoArgsConstructor
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_username", columnList = "username"),
    @Index(name = "idx_refresh_token_expires_at", columnList = "expires_at")
})
public class RefreshToken {

    @Id
    @SnowflakeId
    @Column(name = "token_id")
    private Long id;

    @Column(name = "username", nullable = false, length = 12)
    private String username;

    @Column(name = "refresh_token", nullable = false, length = 500)
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked")
    @Builder.Default
    private Boolean isRevoked = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void revoke() {
        this.isRevoked = true;
    }

    public boolean isValid() {
        return !this.isRevoked && !this.isExpired();
    }
} 