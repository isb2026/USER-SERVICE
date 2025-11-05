package com.lts5.user.payload.request.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "권한 검색 요청")
public class PermissionSearchRequest {

    @Schema(description = "권한 ID", example = "1")
    private Long id;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDelete;

    @Size(max = 100)
    @Schema(description = "권한 코드", example = "purchase:create")
    private String code;

    @Size(max = 255)
    @Schema(description = "설명", example = "사용자 조회 권한")
    private String description;

    @Size(max = 100)
    @Schema(description = "서비스명", example = "purchase-service")
    private String serviceName;

    @Schema(description = "테넌트 ID", example = "10001")
    private Short tenantId;
} 