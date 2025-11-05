package com.lts5.user.repository.role;

import com.lts5.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends 
        JpaRepository<Role, Long>, 
        QuerydslPredicateExecutor<Role>, 
        RoleRepositoryCustom {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
} 