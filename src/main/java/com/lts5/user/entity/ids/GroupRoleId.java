package com.lts5.user.entity.ids;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class GroupRoleId implements Serializable {

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "role_id")
    private Long roleId;

    // 기본 생성자
    public GroupRoleId() {}

    // 매개변수 생성자
    public GroupRoleId(Long groupId, Long roleId) {
        this.groupId = groupId;
        this.roleId = roleId;
    }

    // Getter 메서드들
    public Long getGroupId() {
        return groupId;
    }

    public Long getRoleId() {
        return roleId;
    }

    // Setter 메서드들
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    // equals 메서드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupRoleId that = (GroupRoleId) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(roleId, that.roleId);
    }

    // hashCode 메서드
    @Override
    public int hashCode() {
        return Objects.hash(groupId, roleId);
    }
} 