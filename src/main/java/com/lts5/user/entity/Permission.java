package com.lts5.user.entity;

import com.lts5.user.dto.PermissionDto;
import com.primes.library.audit.KafkaAuditEntityListener;
import com.primes.library.entity.BaseEntity;
import com.primes.library.entity.SnowflakeId;
import com.primes.library.entity.SnowflakeIdEntityListener;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class, KafkaAuditEntityListener.class, SnowflakeIdEntityListener.class})
@Table(name = "permissions", uniqueConstraints = {
    @UniqueConstraint(name = "code", columnNames = {"code"})
})
public class Permission extends BaseEntity {

    @Id
    @SnowflakeId
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @Column(name = "is_delete")
    private Boolean isDelete = false;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    public PermissionDto toDto() {
        return PermissionDto.builder()
                .id(this.id)
                .isDelete(this.isDelete)
                .code(this.code)
                .description(this.description)
                .serviceName(this.serviceName)
                .tenantId(getTenantId())
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }

    public void setDelete() {
        this.isDelete = true;
    }
} 