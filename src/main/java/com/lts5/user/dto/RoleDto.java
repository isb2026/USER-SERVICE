package com.lts5.user.dto;

import com.lts5.user.entity.Role;
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
public class RoleDto extends BaseDto {
    private Long id;
    private Short tenantId;
    private Boolean isDelete;
    private String name;
    private String description;

    public Role toEntity() {
        return Role.builder()
                .id(this.id)
                .tenantId(getTenantId())
                .isDelete(this.isDelete)
                .name(this.name)
                .description(this.description)
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 