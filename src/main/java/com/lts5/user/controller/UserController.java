package com.lts5.user.controller;

import com.lts5.user.dto.UserDto;
import com.lts5.user.payload.request.user.UserSearchRequest;
import com.lts5.user.payload.request.user.UserUpdateRequest;
import com.lts5.user.service.UserService;
import com.primes.library.common.response.CommonResponse;
import com.primes.library.filter.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {
    private final UserService userService;

    @Operation(summary = "사용자 조회")
    @GetMapping("")
    public CommonResponse<Page<UserDto>> search(
            @Valid UserSearchRequest searchRequest,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        
        // TenantContext 확인 및 디버깅
        Short tenantId = TenantContext.getTenantId();
        System.out.println("UserController.search() - 현재 TenantContext: " + tenantId);
        
        // TenantContext가 null이거나 0인 경우 경고
        if (tenantId == null || tenantId == 0) {
            System.err.println("WARNING: TenantContext가 설정되지 않았습니다! tenantId: " + tenantId);
        }
        
        return CommonResponse.createSuccess(userService.search(searchRequest, pageRequest));
    }

    @Operation(summary = "사용자 수정")
    @PutMapping("/{username}")
    public CommonResponse<UserDto> update(
            @PathVariable String username,
            @Valid @RequestBody UserUpdateRequest request) {
        Short tenantId = TenantContext.getTenantId();
        return CommonResponse.createSuccess(userService.update(username, request.toDto(tenantId)));
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/{username}")
    public CommonResponse<?> delete(@PathVariable String username) {
        userService.delete(username);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "사용자 특정 필드 값 전체 조회")
    @GetMapping("/fields/{fieldName}")
    public CommonResponse<List<?>> findAllFieldValues(
            @PathVariable String fieldName,
            @Valid UserSearchRequest searchRequest) {
        return CommonResponse.createSuccess(userService.getFieldValues(fieldName, searchRequest));
    }

    @Operation(summary = "사용자에게 그룹 지정 ")
    @PostMapping("/{id}/groups")
    public CommonResponse<?> addGroupsToUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody List<Long> groupIds) {
        userService.addGroupsToUser(id, groupIds);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "사용자에게서 그룹 해제")
    @DeleteMapping("/{userId}/groups")
    public CommonResponse<?> removeGroupFromUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "그룹 ID", example = "2") @RequestBody List<Long> groupIds) {
        userService.removeGroupFromUser(userId, groupIds);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "사용자에게 직접 역할 지정 ")
    @PostMapping("/{id}/roles")
    public CommonResponse<?> addRolesToUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody List<Long> roleIds) {
        userService.addRolesToUser(id, roleIds);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "사용자에게서 역할 해제")
    @DeleteMapping("/{userId}/roles")
    public CommonResponse<?> removeRoleFromUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "역할 ID", example = "2") @RequestBody List<Long> roleIds) {
        userService.removeRoleFromUser(userId, roleIds);
        return CommonResponse.createSuccessWithNoContent();
    }
} 