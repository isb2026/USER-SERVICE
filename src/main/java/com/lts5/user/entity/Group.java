package com.lts5.user.entity;

import com.lts5.user.dto.GroupDto;
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
@Table(name = "groups", uniqueConstraints = {
    @UniqueConstraint(name = "name", columnNames = {"name"})
})
public class Group extends BaseEntity {

    @Id
    @SnowflakeId
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @Column(name = "is_delete")
    private Boolean isDelete = false;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    public GroupDto toDto() {
        return GroupDto.builder()
                .id(this.id)
                .isDelete(this.isDelete)
                .name(this.name)
                .description(this.description)
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