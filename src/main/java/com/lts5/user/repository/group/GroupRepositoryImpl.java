package com.lts5.user.repository.group;

import com.lts5.user.entity.Group;
import com.lts5.user.payload.request.group.GroupSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.StringUtils;

import static com.lts5.user.entity.QGroup.group;

public class GroupRepositoryImpl extends QuerydslRepositorySupport implements GroupRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public GroupRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Group.class);
        this.queryFactory = queryFactory;
    }
    
    @Override
    public Page<Group> search(GroupSearchRequest searchRequest, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(eqId(searchRequest.getId()))
                .and(eqIsDelete(searchRequest.getIsDelete()))
                .and(containsName(searchRequest.getName()))
                .and(containsDescription(searchRequest.getDescription()));

        List<Group> content = getQuerydsl()
                .applyPagination(pageable, 
                        queryFactory.selectFrom(group)
                                .where(builder))
                .fetch();

        long total = queryFactory.selectFrom(group)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression eqId(Long id) {
        return id != null ? group.id.eq(id) : null;
    }

    private BooleanExpression eqIsDelete(Boolean isDelete) {
        return isDelete != null ? group.isDelete.eq(isDelete) : null;
    }

    private BooleanExpression containsName(String name) {
        return StringUtils.hasText(name) ? group.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression containsDescription(String description) {
        return StringUtils.hasText(description) ? group.description.containsIgnoreCase(description) : null;
    }
} 