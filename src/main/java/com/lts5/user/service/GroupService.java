package com.lts5.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primes.library.common.codes.ErrorCode;
import com.primes.library.common.exceptions.EntityNotFoundException;
import com.lts5.user.payload.request.group.GroupSearchRequest;
import com.lts5.user.repository.group.GroupRepository;
import com.lts5.user.dto.GroupDto;
import com.lts5.user.entity.Group;
import com.lts5.user.entity.GroupRole;
import com.lts5.user.entity.Role;
import com.primes.library.util.DynamicFieldQueryUtil;
import com.primes.library.service.BaseService;
import com.lts5.user.repository.relation.GroupRoleRepository;
import com.lts5.user.repository.role.RoleRepository;
import com.lts5.user.entity.ids.GroupRoleId;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService extends BaseService{

    private final GroupRepository groupRepository;
    private final DynamicFieldQueryUtil dynamicFieldQueryUtil;
    private final GroupRoleRepository groupRoleRepository;
    private final RoleRepository roleRepository;
    @Transactional
    public GroupDto create(GroupDto dto) {
        Group entity = dto.toEntity();
        Group savedEntity = groupRepository.save(entity);
        return savedEntity.toDto();
    }

    @Transactional
    public List<GroupDto> createList(List<GroupDto> dtos) {
        List<Group> entities = dtos.stream()
                .map(GroupDto::toEntity)
                .toList();
        
        List<Group> savedEntities = groupRepository.saveAll(entities);
        return savedEntities.stream()
                .map(Group::toDto)
                .toList();
    }

    @Transactional
    public GroupDto update(Long id, GroupDto dto) {
        dto.setId(id);  // DTO에 ID 설정
        return updateSingle(dto);
    }
    
    @Transactional
    public List<GroupDto> updateAll(List<GroupDto> dtos) {
        return dtos.stream()
                .map(this::updateSingle)
                .toList();
    }

    @Transactional
    public void delete(List<Long> ids) {
        List<Group> existingEntities = groupRepository.findAllById(ids);
        
        if (existingEntities.size() != ids.size()) {
            List<Long> existingIds = existingEntities.stream().map(Group::getId).toList();
            List<Long> notFoundIds = new ArrayList<>(ids);
            notFoundIds.removeAll(existingIds);

            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                String.format("Id가 %s인 Group 데이터가 없습니다.", notFoundIds));
        }

        for (Group entity : existingEntities) {
            entity.setDelete();
        }
        groupRepository.saveAll(existingEntities);
    }

    public List<?> getFieldValues(String fieldName, GroupSearchRequest searchRequest) {
        if (searchRequest == null) {
            return dynamicFieldQueryUtil.getFieldValues(Group.class, fieldName);
        }else{
            return dynamicFieldQueryUtil.getFieldValuesWithFilter(Group.class,  fieldName, searchRequest);
        }
    }

    public Page<GroupDto> search(GroupSearchRequest searchRequest, Pageable pageable) {
        return groupRepository.search(searchRequest, pageable)
                .map(Group::toDto);
    }

    @Transactional
    public void addRolesToGroup(Long id, List<Long> roleIds) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                        String.format("Id 가 %d인 Group 데이터가 없습니다.", id)));
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                            String.format("Id 가 %d인 Role 데이터가 없습니다.", roleId)));
            GroupRole groupRole = GroupRole.builder()
                    .id(new GroupRoleId(id, roleId))
                    .group(group)
                    .role(role)
                    .build();
            groupRoleRepository.save(groupRole);
        }
    }

    @Transactional
    public void removeRolesFromGroup(Long id, List<Long> roleIds) {
        // 그룹 존재 확인
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                        String.format("Id 가 %d인 Group 데이터가 없습니다.", id)));
        
        for (Long roleId : roleIds) {
            // 역할 존재 확인
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                            String.format("Id 가 %d인 Role 데이터가 없습니다.", roleId)));
            // 그룹과 역할 관계 존재 확인
            GroupRole groupRole = groupRoleRepository.findByIdGroupIdAndIdRoleId(id, roleId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR,
                            String.format("Id 가 %d인 GroupRole 데이터가 없습니다.", id)));
            groupRoleRepository.deleteByIdGroupIdAndIdRoleId(id, roleId);
        }
    }

    // ==================== 유틸리티 메서드 ====================

    private GroupDto updateSingle(GroupDto dto) {
        Group entity = groupRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, String.format("Id 가 %d인 Group 데이터가 없습니다.", dto.getId())));
        updateEntityFromDto(entity, dto);
        return entity.toDto();
    }
}
