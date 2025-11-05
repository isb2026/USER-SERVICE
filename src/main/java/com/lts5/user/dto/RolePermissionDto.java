package com.lts5.user.dto;

import com.lts5.user.entity.RolePermission;
import com.lts5.user.entity.Role;
import com.lts5.user.entity.Permission;
import com.lts5.user.entity.ids.RolePermissionId;
import com.primes.library.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissionDto extends BaseDto {
    private Long roleId;
    private Long permissionId;

    public RolePermission toEntity() {
        RolePermissionId id = new RolePermissionId(roleId, permissionId);
        
        return RolePermission.builder()
                .id(id)
                .tenantId(getTenantId())
                .role(Role.builder().id(roleId).build())
                .permission(Permission.builder().id(permissionId).build())
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 