package com.lts5.user.repository.user;

import com.lts5.user.entity.QCode;
import com.lts5.user.entity.User;
import com.lts5.user.payload.request.user.UserSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.lts5.user.entity.QUser.user;

public class UserRepositoryImpl extends QuerydslRepositorySupport implements UserRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public UserRepositoryImpl(JPAQueryFactory queryFactory) {
        super(User.class);
        this.queryFactory = queryFactory;
    }
    
    @Override
    public Page<User> search(UserSearchRequest searchRequest, Pageable pageable) {
        // 디버깅을 위한 로그 추가
        System.out.println("UserRepositoryImpl.search() - 검색 시작");
        
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(eqId(searchRequest.getId()))
                .and(eqIsUse(searchRequest.getIsUse()))
                .and(eqIsDelete(searchRequest.getIsDelete()))
                .and(containsUsername(searchRequest.getUsername()))
                .and(containsName(searchRequest.getName()))
                .and(containsMobileTel(searchRequest.getMobileTel()))
                .and(containsHomeTel(searchRequest.getHomeTel()))
                .and(eqDepartment(searchRequest.getDepartment()))
                .and(eqPartLevel(searchRequest.getPartLevel()))
                .and(eqPartPosition(searchRequest.getPartPosition()))
                .and(containsZipcode(searchRequest.getZipcode()))
                .and(containsAddressMst(searchRequest.getAddressMst()))
                .and(containsAddressDtl(searchRequest.getAddressDtl()))
                .and(eqInDate(searchRequest.getInDate()))
                .and(eqOutDate(searchRequest.getOutDate()))
                .and(eqIsTenantAdmin(searchRequest.getIsTenantAdmin()))
                .and(eqAccountYear(searchRequest.getAccountYear()));

        // JOIN으로 codeName 포함하여 조회 (별칭 사용)
        QCode code1 = new QCode("code1");
        QCode code2 = new QCode("code2");
        QCode code3 = new QCode("code3");

        List<Tuple> results = queryFactory
                .select(user, code1.codeName, code2.codeName, code3.codeName)
                .from(user)
                .leftJoin(code1).on(code1.codeValue.eq(user.department)
                        .and(code1.isDelete.eq(false))
                        .and(code1.isUse.eq(true)))
                .leftJoin(code2).on(code2.codeValue.eq(user.partLevel)
                        .and(code2.isDelete.eq(false))
                        .and(code2.isUse.eq(true)))
                .leftJoin(code3).on(code3.codeValue.eq(user.partPosition)
                        .and(code3.isDelete.eq(false))
                        .and(code3.isUse.eq(true)))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.id.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(user)
                .where(builder)
                .fetchCount();

        // Tuple을 User로 변환하여 반환
        List<User> users = results.stream()
                .map(tuple -> {
                    User userResult = tuple.get(user);
                    String departmentName = tuple.get(code1.codeName);
                    String partLevelName = tuple.get(code2.codeName);
                    String partPositionName = tuple.get(code3.codeName);

                    // convertToCodeName 메서드 사용하여 모든 codeName 적용
                    userResult.convertToCodeName(departmentName, partLevelName, partPositionName);

                    return userResult;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, total);
    }

    private BooleanExpression eqId(Long id) {
        return id != null ? user.id.eq(id) : null;
    }

    private BooleanExpression eqIsUse(Boolean isUse) {
        return isUse != null ? user.isUse.eq(isUse) : null;
    }

    private BooleanExpression eqIsDelete(Boolean isDelete) {
        return isDelete != null ? user.isDelete.eq(isDelete) : null;
    }

    private BooleanExpression containsUsername(String username) {
        return StringUtils.hasText(username) ? user.username.containsIgnoreCase(username) : null;
    }

    private BooleanExpression containsName(String name) {
        return StringUtils.hasText(name) ? user.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression containsMobileTel(String mobileTel) {
        return StringUtils.hasText(mobileTel) ? user.mobileTel.containsIgnoreCase(mobileTel) : null;
    }

    private BooleanExpression containsHomeTel(String homeTel) {
        return StringUtils.hasText(homeTel) ? user.homeTel.containsIgnoreCase(homeTel) : null;
    }

    private BooleanExpression eqDepartment(String department) {
        return StringUtils.hasText(department) ? user.department.eq(department) : null;
    }

    private BooleanExpression eqPartLevel(String partLevel) {
        return StringUtils.hasText(partLevel) ? user.partLevel.eq(partLevel) : null;
    }

    private BooleanExpression eqPartPosition(String partPosition) {
        return StringUtils.hasText(partPosition) ? user.partPosition.eq(partPosition) : null;
    }

    private BooleanExpression containsZipcode(String zipcode) {
        return StringUtils.hasText(zipcode) ? user.zipcode.containsIgnoreCase(zipcode) : null;
    }

    private BooleanExpression containsAddressMst(String addressMst) {
        return StringUtils.hasText(addressMst) ? user.addressMst.containsIgnoreCase(addressMst) : null;
    }

    private BooleanExpression containsAddressDtl(String addressDtl) {
        return StringUtils.hasText(addressDtl) ? user.addressDtl.containsIgnoreCase(addressDtl) : null;
    }

    private BooleanExpression eqInDate(LocalDate inDate) {
        return inDate != null ? user.inDate.eq(inDate) : null;
    }

    private BooleanExpression eqOutDate(LocalDate outDate) {
        return outDate != null ? user.outDate.eq(outDate) : null;
    }

    private BooleanExpression eqIsTenantAdmin(String isTenantAdmin) {
        return StringUtils.hasText(isTenantAdmin) ? user.isTenantAdmin.eq(isTenantAdmin) : null;
    }

    private BooleanExpression eqAccountYear(Short accountYear) {
        return accountYear != null ? user.accountYear.eq(accountYear) : null;
    }
} 