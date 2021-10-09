package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Topic;
import com.bkav.lk.repository.custom.TopicRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bkav.lk.util.StrUtil.convertList;

public class TopicRepositoryImpl implements TopicRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(TopicRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Topic> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT T FROM Topic T ");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Topic.class);
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
        String sql = "select count(T) from Topic T ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    @Override
    public void delete(String ids) {
        String sql = "UPDATE Topic SET status =:status WHERE id IN (" + ids + ")";
        entityManager.createQuery(sql)
                .setParameter("status", Constants.ENTITY_STATUS.DELETED)
                .executeUpdate();
    }

    @Override
    public List<Topic> findByStatus(Integer status) {
        String sql = "SElECT T FROM Topic T WHERE T.status = :status";
        Query query = entityManager.createQuery(sql);
        query.setParameter("status", status);
        return query.getResultList();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where T.status != 0 ";

        if (queryParams.containsKey("keyWord") &&  !StrUtil.isBlank(queryParams.get("keyWord").get(0))) {
            String keyWord = queryParams.get("keyWord").get(0);
            sql += " and (lower(T.code) like lower(:code) or ";
            values.put("code", "%" + keyWord + "%");
            sql += " lower(T.name) like lower(:name))";
            values.put("name", "%" + keyWord + "%");
        }

        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and  T.status = :status";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }
        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0){
                values.put("ids", list);
                sql += " AND T.id IN (:ids)";
            }
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        String sql = " order by ";
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql += "T." + i.replace(",", " ") + ", ";
            }
            sql = sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql += " T.createdDate desc ";
        }
        return sql;
    }
}
