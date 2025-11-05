package com.lts5.user.payload.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "사용자 검색 요청")
public class UserSearchRequest {

    @Schema(description = "사용자 고유번호", example = "1")
    private Long id;

    @Schema(description = "사용 여부", example = "true")
    private Boolean isUse;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDelete;

    @Schema(description = "사용자 계정", example = "dong")
    private String username;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
    @Schema(description = "휴대전화", example = "010-1234-5678")
    private String mobileTel;

    @Pattern(regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$", message = "집 전화번호 형식이 올바르지 않습니다. 예: 02-123-4567")
    @Schema(description = "집전화", example = "02-123-1234")
    private String homeTel;

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

    @Schema(description = "기본주소", example = "인천 남동구 무른모로 21-23")
    private String addressMst;

    @Schema(description = "상세주소", example = "무른모빌라 A동 101호")
    private String addressDtl;

    @Schema(description = "입사일", example = "2025-04-20")
    private LocalDate inDate;

    @Schema(description = "퇴사일", example = "2025-04-28")
    private LocalDate outDate;

    @Schema(description = "테넌트 관리자 여부", example = "0")
    private String isTenantAdmin;

    @Schema(description = "데이터 회계년", example = "2025")
    private Short accountYear;
}