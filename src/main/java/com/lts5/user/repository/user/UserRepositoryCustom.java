package com.lts5.user.repository.user;

import com.lts5.user.entity.User;
import com.lts5.user.payload.request.user.UserSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> search(UserSearchRequest searchRequest, Pageable pageable);
} 