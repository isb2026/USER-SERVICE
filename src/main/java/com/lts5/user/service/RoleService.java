package com.lts5.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lts5.user.dto.RoleDto;
import com.lts5.user.entity.Role;
import com.lts5.user.entity.Permission;
import com.lts5.user.entity.RolePermission;
import com.lts5.user.entity.ids.RolePermissionId;
import com.lts5.user.payload.request.role.RoleSearchRequest;
import com.lts5.user.repository.permission.PermissionRepository;
import com.lts5.user.repository.role.RoleRepository;
import com.lts5.user.repository.relation.RolePermissionRepository;
import com.primes.library.common.codes.ErrorCode;
import com.primes.library.common.exceptions.EntityNotFoundException;
import com.primes.library.service.BaseService;
import com.primes.library.util.DynamicFieldQueryUtil;
import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;

import static com.lts5.user.entity.QRole.role;

@Service
@RequiredArgsConstructor
public class RoleService extends BaseService{

    private final RoleRepository roleRepository;
    private final DynamicFieldQueryUtil dynamicFieldQueryUtil;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    
    public Page<RoleDto> search(RoleSearchRequest searchRequest, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchRequest != null) {
            if (searchRequest.getId() != null) {
                builder.and(role.id.eq(searchRequest.getId()));
            }
            if (searchRequest.getName() != null && !searchRequest.getName().isBlank()) {
                builder.and(role.name.containsIgnoreCase(searchRequest.getName()));
            }
            if (searchRequest.getDescription() != null && !searchRequest.getDescription().isBlank()) {
                builder.and(role.description.containsIgnoreCase(searchRequest.getDescription()));
            }
        }

        // 삭제되지 않은 데이터만 기본 필터
        builder.and(role.isDelete.eq(false));

        return roleRepository.findAll(builder, pageable)
                .map(Role::toDto);
    }

    @Transactional
    public RoleDto create(RoleDto dto) {
        Role entity = dto.toEntity();
        Role saved = roleRepository.save(entity);
        return saved.toDto();
    }

    @Transactional
    public List<RoleDto> createList(List<RoleDto> dtos) {
        List<Role> entities = dtos.stream()
                .map(RoleDto::toEntity)
                .toList();
        List<Role> saved = roleRepository.saveAll(entities);
        return saved.stream().map(Role::toDto).toList();
    }

    @Transactional
    public RoleDto update(Long id, RoleDto dto) {
        dto.setId(id);
        return updateSingle(dto);
    }

    @Transactional
    public List<RoleDto> updateAll(List<RoleDto> dtos) {
        return dtos.stream().map(this::updateSingle).toList();
    }

    @Transactional
    public void delete(List<Long> ids) {
        List<Role> existing = roleRepository.findAllById(ids);
        if (existing.size() != ids.size()) {
            List<Long> existingIds = existing.stream().map(Role::getId).toList();
            List<Long> notFound = new ArrayList<>(ids);
            notFound.removeAll(existingIds);
            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                    String.format("Id가 %s인 Role 데이터가 없습니다.", notFound));
        }

        for (Role entity : existing) {
            entity.setDelete();
        }
        roleRepository.saveAll(existing);
    }

    public List<?> getFieldValues(String fieldName, RoleSearchRequest searchRequest) {
        if (searchRequest == null) {
            return dynamicFieldQueryUtil.getFieldValues(Role.class, fieldName);
        } else {
            return dynamicFieldQueryUtil.getFieldValuesWithFilter(Role.class, fieldName, searchRequest);
        }
    }

    @Transactional
    public void addPermissionsToRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                        String.format("Id 가 %d인 Role 데이터가 없습니다.", roleId)));
        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                            String.format("Id 가 %d인 Permission 데이터가 없습니다.", permissionId)));
            RolePermission rolePermission = RolePermission.builder()
                    .id(new RolePermissionId(roleId, permissionId))
                    .role(role)
                    .permission(permission)
                    .build();
            rolePermissionRepository.save(rolePermission);
        }
    }

    @Transactional
    public void removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                        String.format("Id 가 %d인 Role 데이터가 없습니다.", roleId)));
        for (Long permissionId : permissionIds) {
            rolePermissionRepository.findByIdRoleIdAndIdPermissionId(roleId, permissionId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                        String.format("Id 가 %d인 RolePermission 데이터가 없습니다.", roleId)));
            rolePermissionRepository.deleteByIdRoleIdAndIdPermissionId(roleId, permissionId);
        }
    }

    // ==================== 유틸리티 메서드 ====================
    private RoleDto updateSingle(RoleDto dto) {
        Role entity = roleRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                        String.format("Id 가 %d인 Role 데이터가 없습니다.", dto.getId())));
        updateEntityFromDto(entity, dto);
        return entity.toDto();
    }
}
