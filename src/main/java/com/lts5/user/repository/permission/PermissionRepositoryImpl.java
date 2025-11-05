package com.lts5.user.repository.permission;

import com.lts5.user.entity.Permission;
import com.lts5.user.payload.request.permission.PermissionSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.StringUtils;

import static com.lts5.user.entity.QPermission.permission;

public class PermissionRepositoryImpl extends QuerydslRepositorySupport implements PermissionRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public PermissionRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Permission.class);
        this.queryFactory = queryFactory;
    }
    
    @Override
    public Page<Permission> search(PermissionSearchRequest searchRequest, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(eqId(searchRequest.getId()))
                .and(eqIsDelete(searchRequest.getIsDelete()))
                .and(containsCode(searchRequest.getCode()))
                .and(containsDescription(searchRequest.getDescription()))
                .and(containsServiceName(searchRequest.getServiceName()));

        List<Permission> content = getQuerydsl()
                .applyPagination(pageable, 
                        queryFactory.selectFrom(permission)
                                .where(builder))
                .fetch();

        long total = queryFactory.selectFrom(permission)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression eqId(Long id) {
        return id != null ? permission.id.eq(id) : null;
    }

    private BooleanExpression eqIsDelete(Boolean isDelete) {
        return isDelete != null ? permission.isDelete.eq(isDelete) : null;
    }

    private BooleanExpression containsCode(String code) {
        return StringUtils.hasText(code) ? permission.code.containsIgnoreCase(code) : null;
    }

    private BooleanExpression containsDescription(String description) {
        return StringUtils.hasText(description) ? permission.description.containsIgnoreCase(description) : null;
    }

    private BooleanExpression containsServiceName(String serviceName) {
        return StringUtils.hasText(serviceName) ? permission.serviceName.containsIgnoreCase(serviceName) : null;
    }
} 