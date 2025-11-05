package com.lts5.user.controller;

import com.lts5.user.dto.GroupDto;
import com.lts5.user.payload.request.group.GroupCreateRequest;
import com.lts5.user.payload.request.group.GroupUpdateAllRequest;
import com.lts5.user.payload.request.group.GroupUpdateRequest;
import com.lts5.user.service.GroupService;
import com.primes.library.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import com.lts5.user.payload.request.group.GroupSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/groups")
@Validated
@RequiredArgsConstructor
@Tag(name = "Group", description = "그룹 관리 API")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 조회")
    @GetMapping("")
    public CommonResponse<Page<GroupDto>> search(
            @Valid GroupSearchRequest searchRequest,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return CommonResponse.createSuccess(groupService.search(searchRequest, pageRequest));
    }

    @Operation(summary = "그룹 생성")
    @PostMapping
    public CommonResponse<List<GroupDto>> create(@Valid @RequestBody List<GroupCreateRequest> requests) {
        List<GroupDto> dtos = requests.stream()
            .map(GroupCreateRequest::toDto)
            .toList();
        return CommonResponse.createSuccess(groupService.createList(dtos));
    }

    @Operation(summary = "그룹 수정")
    @PutMapping("/{id}")
    public CommonResponse<GroupDto> update(
            @PathVariable Long id,
            @Valid @RequestBody GroupUpdateRequest request) {
        return CommonResponse.createSuccess(groupService.update(id, request.toDto()));
    }

    @Operation(summary = "그룹 일괄 수정")
    @PutMapping
    public CommonResponse<List<GroupDto>> updateAll(
            @Valid @RequestBody List<GroupUpdateAllRequest> requests) {
        List<GroupDto> dtos = requests.stream()
            .map(GroupUpdateAllRequest::toDto)
            .toList();
        return CommonResponse.createSuccess(groupService.updateAll(dtos));
    }

    @Operation(summary = "그룹 삭제")
    @DeleteMapping
    public CommonResponse<?> delete(@RequestBody List<Long> ids) {
        groupService.delete(ids);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "그룹 특정 필드 값 전체 조회")
    @GetMapping("/fields/{fieldName}")
    public CommonResponse<List<?>> findAllFieldValues(
            @PathVariable String fieldName,
            @Valid GroupSearchRequest searchRequest) {
        return CommonResponse.createSuccess(groupService.getFieldValues(fieldName, searchRequest));
    }

    @Operation(summary = "그룹에 역할 부여")
    @PostMapping("/{groupId}/roles")
    public CommonResponse<?> addRolesToGroup(
            @Parameter(description = "그룹 ID", example = "1") @PathVariable Long groupId,
            @Valid @RequestBody List<Long> roleIds) {
        groupService.addRolesToGroup(groupId, roleIds);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "그룹에서 역할 해제")
    @DeleteMapping("/{groupId}/roles")
    public CommonResponse<?> removeRolesFromGroup(
            @Parameter(description = "그룹 ID", example = "1") @PathVariable Long groupId,
            @Parameter(description = "역할 ID", example = "2") @RequestBody List<Long> roleIds) {
        groupService.removeRolesFromGroup(groupId, roleIds);
        return CommonResponse.createSuccessWithNoContent();
    }
}