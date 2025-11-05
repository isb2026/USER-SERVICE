package com.lts5.user.dto;

import com.lts5.user.entity.UserRole;
import com.lts5.user.entity.User;
import com.lts5.user.entity.Role;
import com.lts5.user.entity.ids.UserRoleId;
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
public class UserRoleDto extends BaseDto {
    private Long userId;
    private Long roleId;

    public UserRole toEntity() {
        UserRoleId id = new UserRoleId(userId, roleId);
        
        return UserRole.builder()
                .id(id)
                .tenantId(getTenantId())
                .user(User.builder().id(userId).build())
                .role(Role.builder().id(roleId).build())
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 