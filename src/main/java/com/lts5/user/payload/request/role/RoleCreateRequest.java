package com.lts5.user.payload.request.role;

import com.lts5.user.dto.RoleDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "역할 생성 요청")
public class RoleCreateRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "역할명", example = "ADMIN")
    private String name;

    @Size(max = 255)
    @Schema(description = "설명", example = "관리자 역할")
    private String description;

    public RoleDto toDto() {
        return RoleDto.builder()
                .name(name)
                .description(description)
                .build();
    }
} 