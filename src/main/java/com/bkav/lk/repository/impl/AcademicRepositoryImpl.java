package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Academic;
import com.bkav.lk.repository.custom.AcademicRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcademicRepositoryImpl implements AcademicRepositoryCustom {


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Academic> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT A FROM Academic A ");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Academic.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }


    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(A) from Academic A ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where 1 = 1 ";
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and A.status =: status";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        } else {
            sql += " and A.status <> :status";
            values.put("status", Constants.ENTITY_STATUS.DELETED);
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (lower(A.name) like lower(:keyword)" +
                    " or lower(A.status) like lower(:keyword) " +
                    " or lower(A.code) like lower(:keyword) " +
                    "or lower(A.description) like lower(:keyword))";
            values.put("keyword", "%" + queryParams.get("keyword").get(0) + "%");
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        String sql = " order by ";
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql += "A." + i.replace(",", " ") + ", ";
            }
            sql = sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql += " A.createdDate desc ";
        }
        return sql;
    }
}
