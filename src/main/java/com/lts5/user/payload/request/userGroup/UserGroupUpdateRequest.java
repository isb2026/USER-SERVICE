package com.lts5.user.payload.request.userGroup;

import com.lts5.user.dto.UserGroupDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "그룹 수정 요청")
public class UserGroupUpdateRequest {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDelete;

    public UserGroupDto toDto() {
        return UserGroupDto.builder()
                .groupId(groupId)
                .isDelete(isDelete)
                .build();
    }
} 