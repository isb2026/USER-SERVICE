package com.lts5.user.service;

import com.lts5.user.dto.UserDto;
import com.lts5.user.entity.Group;
import com.lts5.user.entity.Role;
import com.lts5.user.entity.User;
import com.lts5.user.entity.UserGroup;
import com.lts5.user.entity.UserRole;
import com.lts5.user.entity.ids.UserGroupId;
import com.lts5.user.entity.ids.UserRoleId;

import com.lts5.user.payload.request.user.UserSearchRequest;
import com.lts5.user.repository.group.GroupRepository;
import com.lts5.user.repository.role.RoleRepository;
import com.lts5.user.repository.relation.UserGroupRepository;
import com.lts5.user.repository.relation.UserRoleRepository;
import com.lts5.user.repository.user.UserRepository;
import com.primes.library.common.codes.ErrorCode;
import com.primes.library.common.exceptions.EntityNotFoundException;
import com.primes.library.service.BaseService;
import com.primes.library.util.DynamicFieldQueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService extends BaseService {
    private final UserRepository userRepository;
    private final DynamicFieldQueryUtil dynamicFieldQueryUtil;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public UserDto update(String username, UserDto dto) {
        User user = userRepository.findByUsernameAndTenantIdAndIsDeleteFalse(username, dto.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));

        updateEntityFromDto(user, dto);
        return user.toDto();
    }

    @Transactional
    public void delete(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR);
        }

        userRepository.deleteByUsername(username);
    }
    
    public List<?> getFieldValues(String fieldName, UserSearchRequest searchRequest) {
        if (searchRequest == null) {
            return dynamicFieldQueryUtil.getFieldValues(User.class, fieldName);
        }else{
            return dynamicFieldQueryUtil.getFieldValuesWithFilter(User.class, fieldName, searchRequest);
        }
    }

    public Page<UserDto> search(UserSearchRequest searchRequest, Pageable pageable) {
        // 기존 search 메서드 사용하여 사용자 조회
        Page<User> users = userRepository.search(searchRequest, pageable);
        
        // 각 사용자에 대해 코드 이름 설정
        return users.map(user -> {
            UserDto userDto = user.toDto();
            // department, partLevel, partPosition은 이미 codeName으로 설정되어 있음
            return userDto;
        });
    }

    @Transactional
    public void addGroupsToUser(Long userId, List<Long> groupIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));
        for (Long groupId : groupIds) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 그룹입니다."));
            
            // 기존 관계가 있는지 확인 (중복 방지)
            Optional<UserGroup> existingUserGroup = userGroupRepository.findByIdUserIdAndIdGroupId(userId, groupId);
            
            if (existingUserGroup.isPresent()) {
                // 이미 관계가 존재하는 경우 기존 관계 반환
                continue;
            }
            
            // 새로운 관계 생성
            UserGroupId userGroupId = new UserGroupId(userId, groupId);
            UserGroup userGroup = UserGroup.builder()
                .id(userGroupId)
                .user(user)
                .group(group)
                .build();
        
            userGroupRepository.save(userGroup);
        }
    }

    @Transactional
    public void removeGroupFromUser(Long userId, List<Long> groupIds) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));
        for  (Long groupId : groupIds) {
            // 그룹 존재 확인
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 그룹입니다."));
            
            // UserGroup 관계 확인 후 물리적 삭제
            UserGroup userGroup = userGroupRepository.findByIdUserIdAndIdGroupId(userId, groupId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "사용자와 그룹 간의 관계가 존재하지 않습니다."));
            
            // 물리적 삭제 (카프카 감사 로그에 DELETE 액션으로 기록됨)
            userGroupRepository.delete(userGroup);
        }
    }


    @Transactional
    public void addRolesToUser(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 역할입니다."));

        // 기존 관계가 있는지 확인 (중복 방지)
        Optional<UserRole> existingUserRole = userRoleRepository.findByIdUserIdAndIdRoleId(userId, roleId);
        
        if (existingUserRole.isPresent()) {
            // 이미 관계가 존재하는 경우 기존 관계 반환
            continue;
        }

        // 새로운 관계 생성
        UserRoleId userRoleId = new UserRoleId(userId, roleId);
        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(user)
                .role(role)
                .build();
        
        userRoleRepository.save(userRole);
        }
    }

    @Transactional
    public void removeRoleFromUser(Long userId, List<Long> roleIds) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 사용자입니다."));
        for (Long roleId : roleIds) {
            // 역할 존재 확인
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "존재하지 않는 역할입니다."));
            
            // UserRole 관계 확인 후 물리적 삭제
            UserRole userRole = userRoleRepository.findByIdUserIdAndIdRoleId(userId, roleId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND_ERROR, "사용자와 역할 간의 관계가 존재하지 않습니다."));
        
            // 물리적 삭제 (카프카 감사 로그에 DELETE 액션으로 기록됨)
            userRoleRepository.delete(userRole);
        }
    }
} 