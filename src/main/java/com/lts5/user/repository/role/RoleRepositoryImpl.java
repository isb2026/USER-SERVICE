package com.lts5.user.repository.role;

import com.lts5.user.entity.Role;
import com.lts5.user.payload.request.role.RoleSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.StringUtils;

import static com.lts5.user.entity.QRole.role;

public class RoleRepositoryImpl extends QuerydslRepositorySupport implements RoleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public RoleRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Role.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Role> search(RoleSearchRequest searchRequest, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(eqId(searchRequest.getId()))
                .and(containsName(searchRequest.getName()))
                .and(containsDescription(searchRequest.getDescription()))
                .and(eqIsDelete(false));

        List<Role> content = getQuerydsl()
                .applyPagination(pageable,
                        queryFactory.selectFrom(role)
                                .where(builder))
                .fetch();

        long total = queryFactory.selectFrom(role)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression eqId(Long id) {
        return id != null ? role.id.eq(id) : null;
    }

    private BooleanExpression eqIsDelete(Boolean isDelete) {
        return isDelete != null ? role.isDelete.eq(isDelete) : null;
    }

    private BooleanExpression containsName(String name) {
        return StringUtils.hasText(name) ? role.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression containsDescription(String description) {
        return StringUtils.hasText(description) ? role.description.containsIgnoreCase(description) : null;
    }
} 