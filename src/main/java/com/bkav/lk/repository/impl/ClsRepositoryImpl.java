package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Cls;
import com.bkav.lk.repository.custom.ClsRepositoryCustom;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bkav.lk.util.StrUtil.convertList;

public class ClsRepositoryImpl implements ClsRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Cls> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT C FROM Cls C ");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Cls.class);
        values.forEach(query::setParameter);
        if(queryParams.containsKey("pageIsNull")){
            pageable = null;
        }
        if(pageable != null){
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(C) from Cls C";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where (C.status != 0) ";
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and C.status = :status ";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (lower(C.clsName) like lower(:keyword) or lower(C.clsCode) like lower(:keyword)) ";
            values.put("keyword", "%" + queryParams.get("keyword").get(0) + "%");
        }
        
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and C.healthFacilityId = :healthFacilityId ";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }
        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0) {
                values.put("ids", list);
                sql += " AND C.id IN (:ids)";
            }
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder( " order by ");
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql.append("C." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql.append(" C.createdDate desc ");
        }
        return sql.toString();
    }

}
