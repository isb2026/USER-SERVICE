package com.lts5.user.repository.group;

import com.lts5.user.entity.Group;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends 
        JpaRepository<Group, Long>, 
        QuerydslPredicateExecutor<Group>, 
        GroupRepositoryCustom {
    Optional<Group> findByName(String name);
    boolean existsByName(String name);
} 