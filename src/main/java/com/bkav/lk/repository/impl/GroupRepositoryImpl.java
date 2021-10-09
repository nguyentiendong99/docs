package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Group;
import com.bkav.lk.repository.custom.GroupRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupRepositoryImpl implements GroupRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Group> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT G, (CASE WHEN G.status = " + Constants.ENTITY_STATUS.ACTIVE + " THEN 1 " +
                "WHEN G.status = " + Constants.ENTITY_STATUS.DEACTIVATE + " THEN 2 " +
                "WHEN G.status = " + Constants.ENTITY_STATUS.DELETED + " THEN 3 ELSE 4 END) AS orderField FROM Group G");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString());
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
        String sql = "select count(G) from Group G ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    @Override
    public void delete(Long id) {
        String sql = "UPDATE Group SET status =:status WHERE id =:id";
        entityManager.createQuery(sql)
                .setParameter("status", Constants.ENTITY_STATUS.DELETED)
                .setParameter("id", id)
                .executeUpdate();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where G.status != -1";
        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (G.groupName like :keyword or G.keyword like :keyword or G.createdBy like :keyword or G.note like :keyword)";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and G.status =: status";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }
        if (queryParams.containsKey("createdBy") && !StrUtil.isBlank(queryParams.get("createdBy").get(0))) {
            sql += " and G.createdBy =: createdBy";
            values.put("createdBy", queryParams.get("createdBy").get(0));
        }
        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant startDate;
            startDate = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            if (startDate != null) {
                sql += " and  G.createdDate >= :startDate";
                values.put("startDate", startDate);
            }
        }
        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            Instant endDate;
            endDate = DateUtils.parseEndOfDay(queryParams.get("endDate").get(0));
            if (endDate != null) {
                sql += " and  G.createdDate <= :endDate";
                values.put("endDate", endDate);
            }
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        String sql = " order by ";
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql += "G." + i.replace(",", " ") + ", ";
            }
            sql = sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql += " orderField ASC, G.lastModifiedDate desc ";
        }
        return sql;
    }
}
