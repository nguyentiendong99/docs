package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.repository.custom.ActivityLogRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActivityLogRepositoryImpl implements ActivityLogRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ActivityLog> search(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT A FROM ActivityLog A");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), ActivityLog.class);
        values.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public List<ActivityLog> searchForManagement(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT A FROM ActivityLog A");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), ActivityLog.class);
        values.forEach(query::setParameter);
        if(queryParams.containsKey("pageIsNull")){
            pageable = null;
        }
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(A) from ActivityLog A";
        Map<String, Object> values = new HashMap<String, Object>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private  String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values){
        String sql = " Where 1=1";
        if (queryParams.containsKey("userId") && !StrUtil.isBlank(queryParams.get("userId").get(0))) {
            sql += " and userId = :userId ";
            values.put("userId",  Long.parseLong(queryParams.get("userId").get(0)));
        }

        if (queryParams.containsKey("contentId") && !StrUtil.isBlank(queryParams.get("contentId").get(0))) {
            sql += " and contentId = :contentId ";
            values.put("contentId",  Long.parseLong(queryParams.get("contentId").get(0)));
        }

        if (queryParams.containsKey("contentType") && !StrUtil.isBlank(queryParams.get("contentType").get(0))) {
            sql += " and contentType = :contentType ";
            values.put("contentType",  Integer.parseInt(queryParams.get("contentType").get(0)));
        }

        if (queryParams.containsKey("contentTypes") && !StrUtil.isBlank(queryParams.get("contentTypes").get(0))) {
            sql += " and contentType IN (:contentTypes) ";
            List<Integer> contentTypes = Stream.of(queryParams.get("contentTypes").get(0).split(",")).map(item -> Integer.valueOf(item.trim())).collect(Collectors.toList());
            values.put("contentTypes",  contentTypes);
        }

        if (queryParams.containsKey("actionType") && !StrUtil.isBlank(queryParams.get("actionType").get(0))) {
            sql += " and A.actionType = :actionType ";
            values.put("actionType", Integer.parseInt(queryParams.get("actionType").get(0)));
        }

        if (queryParams.containsKey("actionTypes") && !StrUtil.isBlank(queryParams.get("actionTypes").get(0))) {
            sql += " and A.actionType IN (:actionTypes) ";
            List<Integer> actionTypes = Stream.of(queryParams.get("actionTypes").get(0).split(",")).map(item -> Integer.valueOf(item.trim())).collect(Collectors.toList());
            values.put("actionTypes", actionTypes);
        }

        if (queryParams.containsKey("action") && !StrUtil.isBlank(queryParams.get("action").get(0))) {
            sql += " and A.actionType =: action ";
            values.put("action",   Integer.parseInt(queryParams.get("action").get(0)));
        }

        if (queryParams.containsKey("module") && !StrUtil.isBlank(queryParams.get("module").get(0))) {
            sql += " and A.contentType =: module ";
            values.put("module",   Integer.parseInt(queryParams.get("module").get(0)));
        }

        if (queryParams.containsKey("doctorId") && !StrUtil.isBlank(queryParams.get("doctorId").get(0))) {
            sql += " and A.contentType = " + Constants.CONTENT_TYPE.DOCTOR + " and A.contentId =: doctorId ";
            values.put("doctorId",   Long.parseLong(queryParams.get("doctorId").get(0)));
        }

        if (queryParams.containsKey("createdBy") && !StrUtil.isBlank(queryParams.get("createdBy").get(0))) {
            sql += " and lower(A.createdBy) = lower(:createdBy) ";
            values.put("createdBy",  queryParams.get("createdBy").get(0));
        }

        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            sql += " and A.createdDate  >= :startDate";
            LocalDate startDate = LocalDate.parse(queryParams.get("startDate").get(0));
            values.put("startDate", startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            sql += " and A.createdDate  <= :endDate";
            LocalDate endDate = LocalDate.parse(queryParams.get("endDate").get(0));
            // +1day do search trong 2 ngày liên tiếp nhưng thực tế chỉ search trong 1 ngày.
            values.put("endDate", endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }


        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" order by ");
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql.append("A." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql.append(" A.createdDate desc ");
        }
        return sql.toString();
    }
}


