package com.lts5.user.repository.auth;

import com.lts5.user.entity.User;
import com.lts5.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthRepositoryImpl implements AuthRepository {
    
    private final UserRepository userRepository;
    
    @Override
    public boolean existsByUsernameAndTenantId(String username, Short tenantId) {
        // Hibernate Filter가 동작하지 않는 Auth 요청에서는 수동으로 테넌트 ID를 추가하여 조회
        return userRepository.existsByUsernameAndTenantId(username, tenantId);
    }
    
    @Override
    public Optional<User> findByUsernameAndTenantIdAndIsDeleteFalse(String username, Short tenantId) {
        // Hibernate Filter가 동작하지 않는 Auth 요청에서는 수동으로 테넌트 ID를 추가하여 조회
        return userRepository.findByUsernameAndTenantIdAndIsDeleteFalse(username, tenantId);
    }
    
    @Override
    public User save(User user) {
        // 사용자 저장 시 테넌트 ID가 이미 설정되어 있어야 함
        return userRepository.save(user);
    }
}
