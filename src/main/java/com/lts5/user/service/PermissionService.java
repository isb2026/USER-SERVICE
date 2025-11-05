package com.lts5.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lts5.user.dto.PermissionDto;
import com.lts5.user.entity.Permission;
import com.lts5.user.payload.request.permission.PermissionSearchRequest;
import com.lts5.user.repository.permission.PermissionRepository;
import com.primes.library.common.codes.ErrorCode;
import com.primes.library.common.exceptions.EntityNotFoundException;
import com.primes.library.service.BaseService;
import com.primes.library.util.DynamicFieldQueryUtil;
import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;

import static com.lts5.user.entity.QPermission.permission;

@Service
@RequiredArgsConstructor
public class PermissionService extends BaseService{

    private final PermissionRepository permissionRepository;
    private final DynamicFieldQueryUtil dynamicFieldQueryUtil;

    public Page<PermissionDto> search(PermissionSearchRequest searchRequest, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchRequest != null) {
            if (searchRequest.getId() != null) {
                builder.and(permission.id.eq(searchRequest.getId()));
            }
            if (searchRequest.getIsDelete() != null) {
                builder.and(permission.isDelete.eq(searchRequest.getIsDelete()));
            } else {
                builder.and(permission.isDelete.eq(false));
            }
            if (searchRequest.getCode() != null && !searchRequest.getCode().isBlank()) {
                builder.and(permission.code.containsIgnoreCase(searchRequest.getCode()));
            }
            if (searchRequest.getDescription() != null && !searchRequest.getDescription().isBlank()) {
                builder.and(permission.description.containsIgnoreCase(searchRequest.getDescription()));
            }
            if (searchRequest.getServiceName() != null && !searchRequest.getServiceName().isBlank()) {
                builder.and(permission.serviceName.containsIgnoreCase(searchRequest.getServiceName()));
            }
            if (searchRequest.getTenantId() != null) {
                builder.and(permission.tenantId.eq(searchRequest.getTenantId()));
            }
        } else {
            builder.and(permission.isDelete.eq(false));
        }

        return permissionRepository.findAll(builder, pageable)
                .map(Permission::toDto);
    }

    @Transactional
    public PermissionDto create(PermissionDto dto) {
        Permission entity = dto.toEntity();
        Permission saved = permissionRepository.save(entity);
        return saved.toDto();
    }

    @Transactional
    public List<PermissionDto> createList(List<PermissionDto> dtos) {
        List<Permission> entities = dtos.stream()
                .map(PermissionDto::toEntity)
                .toList();
        List<Permission> saved = permissionRepository.saveAll(entities);
        return saved.stream().map(Permission::toDto).toList();
    }

    @Transactional
    public PermissionDto update(Long id, PermissionDto dto) {
        dto.setId(id);
        return updateSingle(dto);
    }

    @Transactional
    public List<PermissionDto> updateAll(List<PermissionDto> dtos) {
        return dtos.stream().map(this::updateSingle).toList();
    }

    @Transactional
    public void delete(List<Long> ids) {
        List<Permission> existing = permissionRepository.findAllById(ids);
        if (existing.size() != ids.size()) {
            List<Long> existingIds = existing.stream().map(Permission::getId).toList();
            List<Long> notFound = new ArrayList<>(ids);
            notFound.removeAll(existingIds);
            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                    String.format("Id가 %s인 Permission 데이터가 없습니다.", notFound));
        }

        for (Permission entity : existing) {
            entity.setDelete();
        }
        permissionRepository.saveAll(existing);
    }

    public List<?> getFieldValues(String fieldName, PermissionSearchRequest searchRequest) {
        if (searchRequest == null) {
            return dynamicFieldQueryUtil.getFieldValues(Permission.class, fieldName);
        } else {
            return dynamicFieldQueryUtil.getFieldValuesWithFilter(Permission.class, fieldName, searchRequest);
        }
    }

    // ==================== 유틸리티 메서드 ====================
    private PermissionDto updateSingle(PermissionDto dto) {
        Permission entity = permissionRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                        String.format("Id 가 %d인 Permission 데이터가 없습니다.", dto.getId())));
        updateEntityFromDto(entity, dto);
        return entity.toDto();
    }
}
