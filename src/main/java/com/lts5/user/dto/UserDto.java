package com.lts5.user.dto;

import com.lts5.user.entity.User;
import com.primes.library.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends BaseDto {
    private Long id;
    private Short tenantId;
    private String username;
    private String password;
    private String name;
    private String email;
    private String mobileTel;
    private String homeTel;
    private String profileImage;
    private String department;
    private String partLevel;
    private String partPosition;
    private String zipcode;
    private String addressMaster;
    private String addressDetail;
    private LocalDate inDate;
    private LocalDate outDate;
    private String isTenantAdmin;

    public User toEntity() {
        return User.builder()
                .id(this.id)
                .tenantId(this.tenantId)
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
                .addressMst(this.addressMaster)
                .addressDtl(this.addressDetail)
                .inDate(this.inDate)
                .outDate(this.outDate)
                .isTenantAdmin(this.isTenantAdmin)
                .accountYear(getAccountYear())
                .isUse(this.isUse != null ? this.isUse : true)
                .isDelete(this.isDelete != null ? this.isDelete : false)
                .build();
    }
} 