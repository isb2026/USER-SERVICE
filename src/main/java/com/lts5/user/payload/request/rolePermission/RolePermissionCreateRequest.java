package com.lts5.user.payload.request.rolePermission;

import com.lts5.user.dto.RolePermissionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "권한 생성 요청")
public class RolePermissionCreateRequest {

    @Schema(description = "역할 ID", example = "1")
    private Long roleId;

    @Schema(description = "권한 ID", example = "1")
    private Long permissionId;

    public RolePermissionDto toDto() {
        return RolePermissionDto.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .build();
    }
} 