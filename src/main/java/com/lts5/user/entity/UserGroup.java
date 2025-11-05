package com.lts5.user.entity;

import com.lts5.user.dto.UserGroupDto;
import com.lts5.user.entity.ids.UserGroupId;
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
@Table(name = "user_groups")
public class UserGroup extends BaseEntity {

    @EmbeddedId
    private UserGroupId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    public UserGroupDto toDto() {
        return UserGroupDto.builder()
                .userId(user.getId())
                .groupId(group.getId())
                .tenantId(getTenantId())
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 