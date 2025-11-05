package com.lts5.user.payload.request.role;

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
@Schema(description = "역할 검색 요청")
public class RoleSearchRequest {

    @Schema(description = "역할 ID", example = "1")
    private Long id;

    @Size(max = 100)
    @Schema(description = "역할명", example = "ADMIN")
    private String name;

    @Size(max = 255)
    @Schema(description = "설명", example = "관리자 역할")
    private String description;
} 