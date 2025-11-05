package com.lts5.user.payload.request.userRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "역할 검색 요청")
public class UserRoleSearchRequest {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "역할 ID", example = "1")
    private Long roleId;

} 