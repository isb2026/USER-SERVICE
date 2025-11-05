package com.lts5.user.repository.relation;

import com.lts5.user.entity.RolePermission;
import com.lts5.user.entity.ids.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId>, QuerydslPredicateExecutor<RolePermission> {
    List<RolePermission> findAllByIdRoleId(Long roleId);
    List<RolePermission> findAllByIdPermissionId(Long permissionId);
    void deleteByIdRoleIdAndIdPermissionId(Long roleId, Long permissionId);
    Optional<RolePermission> findByIdRoleIdAndIdPermissionId(Long roleId, Long permissionId);
} 