package com.lts5.user.dto;

import com.lts5.user.entity.GroupRole;
import com.lts5.user.entity.Group;
import com.lts5.user.entity.Role;
import com.lts5.user.entity.ids.GroupRoleId;
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
public class GroupRoleDto extends BaseDto {
    private Long groupId;
    private Long roleId;

    public GroupRole toEntity() {
        GroupRoleId id = new GroupRoleId(groupId, roleId);
        
        return GroupRole.builder()
                .id(id)
                .tenantId(getTenantId())
                .group(Group.builder().id(groupId).build())
                .role(Role.builder().id(roleId).build())
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 