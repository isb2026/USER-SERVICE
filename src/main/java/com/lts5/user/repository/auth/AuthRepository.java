package com.lts5.user.repository.auth;

import com.lts5.user.entity.User;

import java.util.Optional;

public interface AuthRepository {
    
    /**
     * 사용자명과 테넌트 ID로 사용자 존재 여부 확인
     * @param username 사용자명
     * @param tenantId 테넌트 ID
     * @return 존재 여부
     */
    boolean existsByUsernameAndTenantId(String username, Short tenantId);
    
    /**
     * 사용자명과 테넌트 ID로 활성 사용자 조회
     * @param username 사용자명
     * @param tenantId 테넌트 ID
     * @return 사용자 정보
     */
    Optional<User> findByUsernameAndTenantIdAndIsDeleteFalse(String username, Short tenantId);
    
    /**
     * 사용자 저장
     * @param user 저장할 사용자 정보
     * @return 저장된 사용자 정보
     */
    User save(User user);
}
