package com.lts5.user.dto;

import com.lts5.user.entity.UserGroup;
import com.lts5.user.entity.User;
import com.lts5.user.entity.Group;
import com.lts5.user.entity.ids.UserGroupId;
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
public class UserGroupDto extends BaseDto {
    private Long userId;
    private Long groupId;
    
    public UserGroup toEntity() {
        UserGroupId id = new UserGroupId(userId, groupId);
        
        return UserGroup.builder()
                .id(id)
                .tenantId(getTenantId())
                .user(User.builder().id(userId).build())
                .group(Group.builder().id(groupId).build())
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 