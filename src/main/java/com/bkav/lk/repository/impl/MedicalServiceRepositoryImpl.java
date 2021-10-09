package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.MedicalService;
import com.bkav.lk.repository.custom.MedicalServiceRepositoryCustom;
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

public class MedicalServiceRepositoryImpl implements MedicalServiceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MedicalService> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT M FROM MedicalService M");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams,values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), MedicalService.class);
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
        String sql = "select count(M) from MedicalService M";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where (M.status = 1 OR M.status = 2) ";
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql = " where M.status = :status ";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (lower(M.name) like lower(:keyword) or lower(M.code) like lower(:keyword)) ";
            values.put("keyword", "%" + queryParams.get("keyword").get(0) + "%");
        } else {
            if (queryParams.containsKey("name") && !StrUtil.isBlank(queryParams.get("name").get(0))) {
                sql += " and lower(M.name) like lower(:name) ";
                values.put("name", "%" + queryParams.get("name").get(0) + "%");
            }

            if (queryParams.containsKey("code") && !StrUtil.isBlank(queryParams.get("code").get(0))) {
                sql += " and lower(M.code) like lower(:code) ";
                values.put("code", "%" + queryParams.get("code").get(0) + "%");
            }
        }

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and M.healthFacilities.id = :healthFacilityId ";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }
        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0) {
                values.put("ids", list);
                sql += " AND M.id IN (:ids)";
            }
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder( " order by ");
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql.append("M." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql.append(" M.createdDate desc ");
        }
        return sql.toString();
    }

}
