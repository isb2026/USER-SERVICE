package com.lts5.user.payload.request.group;

import com.lts5.user.dto.GroupDto;
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
@Schema(description = "그룹 수정 요청")
public class GroupUpdateRequest {

    @Size(max = 100)
    @Schema(description = "그룹명", example = "Developers")
    private String name;

    @Size(max = 255)
    @Schema(description = "설명", example = "개발자 그룹")
    private String description;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDelete;

    public GroupDto toDto() {
        return GroupDto.builder()
                .name(name)
                .description(description)
                .isDelete(isDelete)
                .build();
    }
} 