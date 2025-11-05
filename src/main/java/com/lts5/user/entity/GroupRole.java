package com.lts5.user.entity;

import com.lts5.user.dto.GroupRoleDto;
import com.lts5.user.entity.ids.GroupRoleId;
import com.primes.library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_roles")
public class GroupRole extends BaseEntity {

    @EmbeddedId
    private GroupRoleId id;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    public GroupRoleDto toDto() {
        return GroupRoleDto.builder()
                .groupId(group.getId())
                .roleId(role.getId())
                .tenantId(getTenantId())
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 