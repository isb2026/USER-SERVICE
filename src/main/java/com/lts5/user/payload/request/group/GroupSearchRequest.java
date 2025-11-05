package com.lts5.user.payload.request.group;

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
@Schema(description = "그룹 검색 요청")
public class GroupSearchRequest {

    @Schema(description = "그룹 ID", example = "1")
    private Long id;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDelete;

    @Size(max = 100)
    @Schema(description = "그룹명", example = "Developers")
    private String name;

    @Size(max = 255)
    @Schema(description = "설명", example = "개발자 그룹")
    private String description;

    @Schema(description = "테넌트 ID", example = "10001")
    private Short tenantId;
} 