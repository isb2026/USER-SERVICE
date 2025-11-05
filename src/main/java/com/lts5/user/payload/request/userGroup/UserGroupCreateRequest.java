package com.lts5.user.payload.request.userGroup;

import com.lts5.user.dto.UserGroupDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "그룹 생성 요청")
public class UserGroupCreateRequest {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    public UserGroupDto toDto() {
        return UserGroupDto.builder()
                .groupId(groupId)
                .isDelete(false)
                .build();
    }
} 