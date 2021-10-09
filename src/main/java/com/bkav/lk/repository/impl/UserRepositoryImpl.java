package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.User;
import com.bkav.lk.repository.custom.UserRepositoryCustom;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.*;

public class UserRepositoryImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT U FROM User U LEFT JOIN Position P ON U.position.id = P.id");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery());
        Query query = entityManager.createQuery(sql.toString(), User.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(U) FROM User U LEFT JOIN Position P ON U.position.id = P.id");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        Query query = entityManager.createQuery(sql.toString(), Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();

    }

    @Override
    public List<User> getListUser(){
        String sql = "SELECT U from User U";
        Query query = entityManager.createQuery(sql, User.class);
        return query.getResultList();
    }

    //<editor-fold desc="HELPER FUNCTION TO CREATE QUERY">
    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();

        if (queryParams.containsKey("groupId") && !StrUtil.isBlank(queryParams.get("groupId").get(0))) {
            sql.append(" JOIN U.groups G WHERE G.id = :groupId ");
            values.put("groupId", Long.parseLong(queryParams.get("groupId").get(0)));
        }else{
            sql.append(" WHERE 1 = 1 ");
        }

        if(queryParams.containsKey("keyWord") && !StrUtil.isBlank(queryParams.get("keyWord").get(0))) {
            String keyWord = queryParams.get("keyWord").get(0);
            sql.append(" and (lower(U.login) like lower(:login) or ");
            values.put("login", "%" + keyWord + "%");
            sql.append(" lower(U.name) like lower(:name) or ");
            values.put("name", "%" + keyWord + "%");
            sql.append(" lower(U.phoneNumber) like lower(:phoneNumber) or");
            values.put("phoneNumber", "%" + keyWord + "%");
            sql.append(" lower(P.name) like lower(:namePosition) )");
            values.put("namePosition", "%" + keyWord + "%");
        }

        if (queryParams.containsKey("agentId")) {
            sql.append(" AND U.agentId = :agentId ");
            values.put("agentId", Long.parseLong(queryParams.get("agentId").get(0)));
        }
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql.append(" AND U.status = :status ");
            values.put("status", Integer.parseInt(queryParams.get("status").get(0)));
        }

        if (queryParams.containsKey("keywordExactly") && !StrUtil.isBlank(queryParams.get("keywordExactly").get(0))) {
            sql.append(" and (lower(U.phoneNumber) = lower(:keywordExactly) or lower(U.name) = lower(:keywordExactly) or lower(U" +
                    ".email) = lower(:keywordExactly) or lower(U.address) = lower(:keywordExactly) or lower(U.login) = lower(:keywordExactly)) and U.agentId is not null ");
            values.put("keywordExactly", queryParams.get("keywordExactly").get(0));
        }

        if (queryParams.containsKey("activated")) {
            sql.append(" AND U.activated = :activated ");
            values.put("activated", Integer.parseInt(queryParams.get("activated").get(0)));
        }
        if (queryParams.containsKey("login")) {
            sql.append(" AND LOWER(U.login) like LOWER(:login)");
            values.put("login", "%" + queryParams.get("login").get(0) + "%");
        }
        if (queryParams.containsKey("email")) {
            sql.append(" AND LOWER(U.email) LIKE LOWER(:email)");
            values.put("email", "%" + queryParams.get("email").get(0) + "%");
        }
        if (queryParams.containsKey("positionId") && !StrUtil.isBlank(queryParams.get("positionId").get(0))) {
            sql.append(" AND  U.position.id = :positionId");
            values.put("positionId", Long.valueOf(queryParams.get("positionId").get(0)));
        }
        if (queryParams.containsKey("departmentId") && !StrUtil.isBlank(queryParams.get("departmentId").get(0))) {
            sql.append(" AND  U.department.id = :departmentId");
            values.put("departmentId", Long.valueOf(queryParams.get("departmentId").get(0)));
        }

        if (queryParams.containsKey("usernameList") && !StrUtil.isBlank(queryParams.get("usernameList").get(0))) {
            sql.append(" AND U.login in (:usernameList)");
            values.put("usernameList", Arrays.asList(queryParams.get("usernameList").get(0).split(",")));
        }

        if (queryParams.containsKey("userIds") && !StrUtil.isBlank(queryParams.get("userIds").get(0))) {
            List<String> ids = Arrays.asList(queryParams.get("userIds").get(0).split(","));
            List<Long> longIds = new ArrayList<>();
            if (ids.size() > 0) {
                ids.forEach(i -> {
                    longIds.add(Long.valueOf(i));
                });
                sql.append(" AND U.id in (:userIds)");
                values.put("userIds", longIds);
            }
        }


        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant instantFrom = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            if (instantFrom != null) {
                sql.append(" AND U.createdDate >= :startDate ");
                values.put("startDate", instantFrom);
            }
        }

        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            Instant instantTo = DateUtils.parseStartOfDay(queryParams.get("endDate").get(0));
            if (instantTo != null) {
                sql.append(" AND U.createdDate <= :endDate ");
                values.put("endDate", instantTo);
            }
        }
        return sql.toString();
    }

    private StringBuilder createOrderQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append(" order by U.status, U.lastModifiedDate desc ");
        return sql;

    }
    //</editor-fold>


}
