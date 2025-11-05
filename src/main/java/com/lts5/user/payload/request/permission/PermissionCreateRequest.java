package com.lts5.user.payload.request.permission;

import com.lts5.user.dto.PermissionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "권한 생성 요청")
public class PermissionCreateRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "권한 코드", example = "purchase:create")
    private String code;

    @Size(max = 255)
    @Schema(description = "설명", example = "구매 생성 권한")
    private String description;

    @Size(max = 100)
    @Schema(description = "서비스명", example = "purchase-service")
    private String serviceName;

    public PermissionDto toDto() {
        return PermissionDto.builder()
                .code(code)
                .description(description)
                .serviceName(serviceName)
                .build();
    }
} 