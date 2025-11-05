package com.lts5.user.entity;

import com.lts5.user.dto.UserDto;
import com.primes.library.audit.KafkaAuditEntityListener;
import com.primes.library.entity.BaseEntity;
import com.primes.library.entity.SnowflakeId;
import com.primes.library.entity.SnowflakeIdEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class, KafkaAuditEntityListener.class, SnowflakeIdEntityListener.class})
@SuperBuilder
@NoArgsConstructor
@Table(name = "users", indexes = {
    @Index(name = "users_t_id_IDX", columnList = "t_id,is_delete,is_use DESC"),
    @Index(name = "uq_users_tenant_username", columnList = "t_id,username", unique = true)
})
public class User extends BaseEntity {

    @Id
    @SnowflakeId
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false, length = 12)
    private String username;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "mobile_tel", length = 20)
    private String mobileTel;

    @Column(name = "home_tel", length = 20)
    private String homeTel;

    @Column(name = "profile_image", length = 100)
    private String profileImage;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "part_level", length = 50)
    private String partLevel;

    @Column(name = "part_position", length = 50)
    private String partPosition;

    @Column(name = "zipcode", length = 20)
    private String zipcode;

    @Column(name = "address_mst", length = 100)
    private String addressMst;

    @Column(name = "address_dtl", length = 100)
    private String addressDtl;

    @Column(name = "in_date")
    private java.time.LocalDate inDate;

    @Column(name = "out_date")
    private java.time.LocalDate outDate;

    @Column(name = "is_tenant_admin", nullable = false, length = 1)
    @Builder.Default
    private String isTenantAdmin = "0";

    @Column(name = "account_year")
    @Builder.Default
    private Short accountYear = 0;

    @Column(name = "is_delete")
    @Builder.Default
    protected Boolean isDelete = false;

    @Column(name = "is_use")
    @Builder.Default
    protected Boolean isUse = true;

    public UserDto toDto() {
        return UserDto.builder()
                .id(this.id)
                .username(this.username)
                .password(this.password)
                .name(this.name)
                .email(this.email)
                .mobileTel(this.mobileTel)
                .homeTel(this.homeTel)
                .profileImage(this.profileImage)
                .department(this.department)
                .partLevel(this.partLevel)
                .partPosition(this.partPosition)
                .zipcode(this.zipcode)
                .addressMaster(this.addressMst)
                .addressDetail(this.addressDtl)
                .inDate(this.inDate)
                .outDate(this.outDate)
                .isTenantAdmin(this.isTenantAdmin)
                .tenantId(getTenantId())
                .accountYear(this.accountYear)
                .isUse(this.isUse)
                .isDelete(this.isDelete)
                .createdAt(getCreatedAt())
                .createdBy(getCreatedBy())
                .updatedAt(getUpdatedAt())
                .updatedBy(getUpdatedBy())
                .build();
    }

    public void setNewPassword(String password) {
        this.password = password;
    }

    /**
     * 부서, 직급, 직책 필드를 codeName으로 업데이트
     */
    public void convertToCodeName(String departmentName, String partLevelName, String partPositionName) {
        if (departmentName != null) {
            this.department = departmentName;
        }
        if (partLevelName != null) {
            this.partLevel = partLevelName;
        }
        if (partPositionName != null) {
            this.partPosition = partPositionName;
        }
    }
}