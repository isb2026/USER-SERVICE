package com.lts5.user.repository.group;

import com.lts5.user.entity.Group;
import com.lts5.user.payload.request.group.GroupSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupRepositoryCustom {
    Page<Group> search(GroupSearchRequest searchRequest, Pageable pageable);
} 