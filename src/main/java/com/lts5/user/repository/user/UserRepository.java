package com.lts5.user.repository.user;

import com.lts5.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends 
        JpaRepository<User, Long>,
        QuerydslPredicateExecutor<User>,
        UserRepositoryCustom {

    // 사용자 이름으로 사용자 정보를 조회하는 메서드
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndTenantIdAndIsDeleteFalse(String username, Short tenantId);

    // 사용자 이름이 존재하는지 확인하는 메서드
    boolean existsByUsername(String username);

    // 사용자 이름과 테넌트 ID로 존재하는지 확인하는 메서드
    boolean existsByUsernameAndTenantId(String username, Short tenantId);

    @Modifying
    @Query("UPDATE User u SET u.isDelete = true WHERE u.username = :username")
    void deleteByUsername(@Param("username") String username);
} 