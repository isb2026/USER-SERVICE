package com.lts5.user.controller;

import com.lts5.user.dto.PermissionDto;
import com.lts5.user.payload.request.permission.PermissionCreateRequest;
import com.lts5.user.payload.request.permission.PermissionSearchRequest;
import com.lts5.user.payload.request.permission.PermissionUpdateRequest;
import com.lts5.user.payload.request.permission.PermissionUpdateAllRequest;
import com.lts5.user.service.PermissionService;
import com.primes.library.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Validated
@RequiredArgsConstructor
@Tag(name = "Permission", description = "권한 관리 API")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "권한 조회")
    @GetMapping("")
    public CommonResponse<Page<PermissionDto>> search(
            @Valid PermissionSearchRequest searchRequest,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return CommonResponse.createSuccess(permissionService.search(searchRequest, pageRequest));
    }

    @Operation(summary = "권한 생성")
    @PostMapping
    public CommonResponse<List<PermissionDto>> create(@Valid @RequestBody List<PermissionCreateRequest> requests) {
        List<PermissionDto> dtos = requests.stream()
                .map(req -> req.toDto())
                .toList();
        return CommonResponse.createSuccess(permissionService.createList(dtos));
    }

    @Operation(summary = "권한 수정")
    @PutMapping("/{id}")
    public CommonResponse<PermissionDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequest request) {
        return CommonResponse.createSuccess(permissionService.update(id, request.toDto()));
    }

    @Operation(summary = "권한 일괄 수정")
    @PutMapping
    public CommonResponse<List<PermissionDto>> updateAll(
            @Valid @RequestBody List<PermissionUpdateAllRequest> requests) {
        List<PermissionDto> dtos = requests.stream()
                .map(PermissionUpdateAllRequest::toDto)
                .toList();
        return CommonResponse.createSuccess(permissionService.updateAll(dtos));
    }

    @Operation(summary = "권한 삭제")
    @DeleteMapping
    public CommonResponse<?> delete(@RequestBody List<Long> ids) {
        permissionService.delete(ids);
        return CommonResponse.createSuccessWithNoContent();
    }

    @Operation(summary = "권한 특정 필드 값 전체 조회")
    @GetMapping("/fields/{fieldName}")
    public CommonResponse<List<?>> findAllFieldValues(
            @PathVariable String fieldName,
            @Valid PermissionSearchRequest searchRequest) {
        return CommonResponse.createSuccess(permissionService.getFieldValues(fieldName, searchRequest));
    }
}
