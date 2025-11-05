package com.lts5.user.repository.relation;

import java.util.List;
import java.util.Optional;

import com.lts5.user.entity.UserGroup;
import com.lts5.user.entity.ids.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId>, QuerydslPredicateExecutor<UserGroup> {
    List<UserGroup> findAllByIdUserId(Long userId);
    List<UserGroup> findAllByIdGroupId(Long groupId);
    Optional<UserGroup> findByIdUserIdAndIdGroupId(Long userId, Long groupId);
} 