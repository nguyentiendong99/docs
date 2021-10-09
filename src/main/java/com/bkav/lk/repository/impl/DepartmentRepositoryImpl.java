package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Department;
import com.bkav.lk.repository.custom.DepartmentRepositoryCustom;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartmentRepositoryImpl implements DepartmentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Department> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT d FROM Department d ");
        Map<String, Object> values = new HashMap<>();
        sql.append(this.createWhereQuery(queryParams, values));
        sql.append(this.createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Department.class);
        values.forEach(query::setParameter);
        if(queryParams.containsKey("pageIsNull")){
            pageable = null;
        }
        if (pageable != null) {
            if (queryParams.containsKey("page") && queryParams.containsKey("size")) {
                query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
                query.setMaxResults(pageable.getPageSize());
            } else {
                query.setFirstResult(0);
                query.setMaxResults(Integer.MAX_VALUE);
            }
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(d) FROM Department d ");
        Map<String, Object> values = new HashMap<>();
        sql.append(this.createWhereQuery(queryParams, values));
        sql.append(this.createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder("WHERE d.status > 0 AND d.parentId IS NULL ");

        if (queryParams.containsKey("name") && !StrUtil.isBlank(queryParams.get("name").get(0))) {
            sql.append("AND lower(d.name) LIKE lower(:name) ");
            values.put("name", "%" + queryParams.get("name").get(0).trim() + "%");
        }
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql.append(" AND d.status = :status ");
            values.put("status", Integer.parseInt(queryParams.get("status").get(0).trim()));
        }
        return sql.toString();
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("ORDER BY ");
        if (queryParams.containsKey("sort")) {
            List<String> orders = queryParams.get("sort");
            for (String order : orders) {
                sql.append("d." + order.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(", "));
        } else {
            sql.append("d.createdDate DESC");
        }
        return sql.toString();
    }
}
