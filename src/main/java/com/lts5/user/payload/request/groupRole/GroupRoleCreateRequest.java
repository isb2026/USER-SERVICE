package com.lts5.user.payload.request.groupRole;

import com.lts5.user.dto.GroupRoleDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "그룹 생성 요청")
public class GroupRoleCreateRequest {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "역할 ID", example = "1")
    private Long roleId;

    public GroupRoleDto toDto() {
        return GroupRoleDto.builder()
                .groupId(groupId)
                .roleId(roleId)
                .build();
    }
} 