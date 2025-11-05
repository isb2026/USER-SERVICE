package com.lts5.user.payload.request.groupRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "그룹 검색 요청")
public class GroupRoleSearchRequest {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "역할 ID", example = "1")
    private Long roleId;

} 