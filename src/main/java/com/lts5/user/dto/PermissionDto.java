package com.lts5.user.dto;

import com.lts5.user.entity.Permission;
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
public class PermissionDto extends BaseDto {
    private Long id;
    private Short tenantId;
    private Boolean isDelete;
    private String code;
    private String description;
    private String serviceName;

    public Permission toEntity() {
        return Permission.builder()
                .id(this.id)
                .tenantId(getTenantId())
                .isDelete(this.isDelete != null ? this.isDelete : false)
                .code(this.code)
                .description(this.description)
                .serviceName(this.serviceName)
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 