package com.lts5.user.repository.permission;

import com.lts5.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends 
        JpaRepository<Permission, Long>, 
        QuerydslPredicateExecutor<Permission>, 
        PermissionRepositoryCustom {
    Optional<Permission> findByCode(String code);
    boolean existsByCode(String code);
} 