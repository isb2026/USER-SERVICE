package com.lts5.user.repository.relation;

import com.lts5.user.entity.GroupRole;
import com.lts5.user.entity.ids.GroupRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, GroupRoleId>, QuerydslPredicateExecutor<GroupRole> {
    List<GroupRole> findAllByIdGroupId(Long groupId);
    List<GroupRole> findAllByIdRoleId(Long roleId);
    void deleteByIdGroupIdAndIdRoleId(Long groupId, Long roleId);
    Optional<GroupRole> findByIdGroupIdAndIdRoleId(Long groupId, Long roleId);
} 