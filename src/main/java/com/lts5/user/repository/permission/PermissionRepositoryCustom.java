package com.lts5.user.repository.permission;

import com.lts5.user.entity.Permission;
import com.lts5.user.payload.request.permission.PermissionSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionRepositoryCustom {
    Page<Permission> search(PermissionSearchRequest searchRequest, Pageable pageable);
} 