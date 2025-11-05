package com.lts5.user.dto;

import com.lts5.user.entity.Group;
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
public class GroupDto extends BaseDto {
    private Long id;
    private Short tenantId;
    private Boolean isDelete;
    private String name;
    private String description;

    public Group toEntity() {
        return Group.builder()
                .id(this.id)
                .tenantId(getTenantId())
                .isDelete(this.isDelete != null ? this.isDelete : false)
                .name(this.name)
                .description(this.description)
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }
} 