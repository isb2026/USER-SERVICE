package com.lts5.user.repository.role;

import com.lts5.user.entity.Role;
import com.lts5.user.payload.request.role.RoleSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleRepositoryCustom {
    Page<Role> search(RoleSearchRequest searchRequest, Pageable pageable);
} 