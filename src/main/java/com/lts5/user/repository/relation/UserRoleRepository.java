package com.lts5.user.repository.relation;

import com.lts5.user.entity.UserRole;
import com.lts5.user.entity.ids.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId>, QuerydslPredicateExecutor<UserRole> {
    List<UserRole> findAllByIdUserId(Long userId);
    List<UserRole> findAllByIdRoleId(Long roleId);
    Optional<UserRole> findByIdUserIdAndIdRoleId(Long userId, Long roleId);
    void deleteByIdUserIdAndIdRoleId(Long userId, Long roleId);
} 