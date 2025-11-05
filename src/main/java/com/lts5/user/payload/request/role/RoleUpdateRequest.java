package com.lts5.user.payload.request.role;

import com.lts5.user.dto.RoleDto;
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
@Schema(description = "역할 수정 요청")
public class RoleUpdateRequest {

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