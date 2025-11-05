package com.lts5.user.payload.request.userRole;

import com.lts5.user.dto.UserRoleDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "역할 생성 요청")
public class UserRoleCreateRequest {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "역할 ID", example = "1")
    private Long roleId;

    public UserRoleDto toDto() {
        return UserRoleDto.builder()
                .userId(userId)
                .roleId(roleId)
                .build();
    }
} 