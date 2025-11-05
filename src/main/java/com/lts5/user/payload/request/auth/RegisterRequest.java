package com.lts5.user.payload.request.auth;

import com.lts5.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotNull
    @Schema(description = "사용자 계정", example = "dong")
    private String username;

    @NotNull
    @Schema(description = "사용자 비밀번호", example = "dong")
    private String password;

    @NotNull
    @Schema(description = "테넌트 ID", example = "10001")
    private Short tenantId;

    @NotNull
    @Schema(description = "이름", example = "홍길동")
    private String name;

    @NotNull
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
    @Schema(description = "휴대폰번호", example = "010-1234-1234")
    private String mobileTel;

    @Pattern(regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$", message = "집 전화번호 형식이 올바르지 않습니다. 예: 02-123-4567")
    @Schema(description = "집전화번호", example = "02-123-1234")
    private String homeTel;

    @Schema(description = "프로필이미지", example = "test.png")
    private String profileImage;

    @Size(max = 50, message = "부서는 50자 이내로 입력해주세요.")
    @Schema(description = "부서", example = "COM-001-002")
    private String department;

    @Size(max = 50, message = "직급은 50자 이내로 입력해주세요.")
    @Schema(description = "직급", example = "COM-002-004")
    private String partLevel;

    @Size(max = 50, message = "직책은 50자 이내로 입력해주세요.")
    @Schema(description = "직책", example = "COM-003-001")
    private String partPosition;

    @Size(max = 20, message = "우편번호는 20자 이내로 입력해주세요.")
    @Schema(description = "우편번호", example = "632-123")
    private String zipcode;

    @Schema(description = "주소", example = "인천 남동구 무른모로 21-23")
    private String addressMaster;

    @Schema(description = "상세주소", example = "무른모빌라 A동 101호")
    private String addressDetail;

    @Schema(description = "입사일자", example = "2025-04-20")
    private LocalDate inDate;

    @Schema(description = "퇴사일자", example = "2025-04-28")
    private LocalDate outDate;

    @NotNull
    @Schema(description = "테넌트 어드민 여부", example = "0")
    private String isTenantAdmin;

    public UserDto toDto() {
        return UserDto.builder()
                .username(this.username)
                .password(this.password)
                .tenantId(this.tenantId)
                .name(this.name)
                .email(this.email)
                .mobileTel(this.mobileTel)
                .homeTel(this.homeTel)
                .profileImage(this.profileImage)
                .department(this.department)
                .partLevel(this.partLevel)
                .partPosition(this.partPosition)
                .zipcode(this.zipcode)
                .addressMaster(this.addressMaster)
                .addressDetail(this.addressDetail)
                .inDate(this.inDate)
                .outDate(this.outDate)
                .isTenantAdmin(this.isTenantAdmin)
                .build();
    }
}