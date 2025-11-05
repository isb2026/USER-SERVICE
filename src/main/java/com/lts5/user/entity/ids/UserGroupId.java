package com.lts5.user.entity.ids;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class UserGroupId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "group_id")
    private Long groupId;

    // 기본 생성자
    public UserGroupId() {}

    // 매개변수 생성자
    public UserGroupId(Long userId, Long groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    // Getter 메서드들
    public Long getUserId() {
        return userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    // Setter 메서드들
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    // equals 메서드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroupId that = (UserGroupId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(groupId, that.groupId);
    }

    // hashCode 메서드
    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }
} 