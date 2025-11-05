package com.lts5.user.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.lts5.user.dto.RoleDto;
import com.lts5.user.payload.request.role.RoleSearchRequest;
import com.lts5.user.payload.request.role.RoleCreateRequest;
import com.lts5.user.payload.request.role.RoleUpdateRequest;
import com.lts5.user.payload.request.role.RoleUpdateAllRequest;
import com.lts5.user.service.RoleService;
import com.primes.library.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/roles")
@Validated
@RequiredArgsConstructor
@Tag(name = "Role", description = "역할 관리 API")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "역할 조회")
    @GetMapping("")
    public CommonResponse<Page<RoleDto>> search(
            @Valid RoleSearchRequest searchRequest,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return CommonResponse.createSuccess(roleService.search(searchRequest, pageRequest));
    }

    @Operation(summary = "역할 생성")
    @PostMapping
    public CommonResponse<List<RoleDto>> create(@Valid @RequestBody List<RoleCreateRequest> requests) {
        List<RoleDto> dtos = requests.stream()
                .map(req -> req.toDto())
                .toList();
        return CommonResponse.createSuccess(roleService.createList(dtos));
    }

    @Operation(summary = "역할 수정")
    @PutMapping("/{id}")
    public CommonResponse<RoleDto> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        return CommonResponse.createSuccess(roleService.update(id, request.toDto()));
    }

    @Operation(summary = "역할 일괄 수정")
    @PutMapping
    public CommonResponse<List<RoleDto>> updateAll(
            @Valid @RequestBody List<RoleUpdateAllRequest> requests) {
        List<RoleDto> dtos = requests.stream()
                .map(RoleUpdateAllRequest::toDto)
                .toList();
        return CommonResponse.createSuccess(roleService.updateAll(dtos));
    }

    @Operation(summary = "역할 삭제")
    @DeleteMapping
    public CommonResponse<?> delete(@RequestBody List<Long> ids) {
        roleService.delete(ids);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "역할 특정 필드 값 조회")
    @GetMapping("/fields/{fieldName}")
    public CommonResponse<List<?>> findAllFieldValues(
            @PathVariable String fieldName,
            @Valid RoleSearchRequest searchRequest) {
        return CommonResponse.createSuccess(roleService.getFieldValues(fieldName, searchRequest));
    }

    @Operation(summary = "역할에 권한 부여")
    @PostMapping("/{roleId}/permissions")
    public CommonResponse<?> addPermissionsToRole(
            @Parameter(description = "역할 ID", example = "1") @PathVariable Long roleId,
            @Valid @RequestBody List<Long> permissionIds) {
        roleService.addPermissionsToRole(roleId, permissionIds);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "역할에서 권한 해제")
    @DeleteMapping("/{roleId}/permissions")
    public CommonResponse<?> removePermissionsFromRole(
            @Parameter(description = "역할 ID", example = "1") @PathVariable Long roleId,
            @Parameter(description = "권한 ID", example = "2") @RequestBody List<Long> permissionIds) {
        roleService.removePermissionsFromRole(roleId, permissionIds);
        return CommonResponse.createSuccessWithNoContent();
    }
}
