package com.lts5.user.payload.request.group;

import com.lts5.user.dto.GroupDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "그룹 생성 요청")
public class GroupCreateRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "그룹명", example = "Developers")
    private String name;

    @Size(max = 255)
    @Schema(description = "설명", example = "개발자 그룹")
    private String description;

    public GroupDto toDto() {
        return GroupDto.builder()
                .name(name)
                .description(description)
                .build();
    }
} 