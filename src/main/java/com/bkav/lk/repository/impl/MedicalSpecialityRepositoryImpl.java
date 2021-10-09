package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.MedicalSpeciality;
import com.bkav.lk.repository.custom.MedicalSpecialityRepositoryCustom;
import com.bkav.lk.util.Constants;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MedicalSpecialityRepositoryImpl implements MedicalSpecialityRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String ADVANCED_SEARCH_KEYWORD = "advancedSearch";

    @Override
    public List<MedicalSpeciality> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT ms FROM MedicalSpeciality ms ");
        Map<String, Object> values = new HashMap<>();
        boolean advancedSearch = false;
        if (queryParams.containsKey(ADVANCED_SEARCH_KEYWORD) && !StringUtils.isEmpty(queryParams.get(ADVANCED_SEARCH_KEYWORD).get(0))) {
            advancedSearch = Boolean.parseBoolean(queryParams.get(ADVANCED_SEARCH_KEYWORD).get(0));
        }

        if (advancedSearch) {
            sql.append(this.createAdvancedWhereQuery(queryParams, values));
        } else {
            sql.append(this.createWhereQuery(queryParams, values));
        }
        sql.append(this.createOrderQuery(queryParams));

        Query query = entityManager.createQuery(sql.toString(), MedicalSpeciality.class);
        values.forEach(query::setParameter);

        if (Objects.nonNull(pageable)) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(ms) FROM MedicalSpeciality ms ");
        Map<String, Object> values = new HashMap<>();
        boolean advancedSearch = false;
        if (queryParams.containsKey(ADVANCED_SEARCH_KEYWORD) && !StringUtils.isEmpty(queryParams.get(ADVANCED_SEARCH_KEYWORD).get(0))) {
            advancedSearch = Boolean.parseBoolean(queryParams.get(ADVANCED_SEARCH_KEYWORD).get(0));
        }

        if (advancedSearch) {
            sql.append(this.createAdvancedWhereQuery(queryParams, values));
        } else {
            sql.append(this.createWhereQuery(queryParams, values));
        }

        Query query = entityManager.createQuery(sql.toString(), Long.class);
        values.forEach(query::setParameter);

        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        StringBuilder subSql = new StringBuilder();

        if (queryParams.containsKey("healthFacilityId") && !StringUtils.isEmpty(queryParams.get("healthFacilityId").get(0))) {
            sql.append("WHERE ms.healthFacilities.id = :healthFacilityId ");
            values.put("healthFacilityId", Long.parseLong(queryParams.get("healthFacilityId").get(0)));
        }
        if (queryParams.containsKey("status") && !StringUtils.isEmpty(queryParams.get("status").get(0))) {
            sql.append("AND ms.status = :status ");
            values.put("status", Integer.parseInt(queryParams.get("status").get(0)));
        } else {
            sql.append("AND ms.status > :status ");
            values.put("status", Constants.ENTITY_STATUS.DELETED);
        }

        if (queryParams.containsKey("keyword") && !StringUtils.isEmpty(queryParams.get("keyword").get(0))) {
            subSql.append("ms.code LIKE :code ");
            values.put("code", "%" + queryParams.get("keyword").get(0).trim() + "%");

            subSql.append("OR ms.name LIKE :name ");
            values.put("name", "%" + queryParams.get("keyword").get(0).trim() + "%");
        }

        if (!StringUtils.isEmpty(subSql.toString().trim())) {
            sql.append("AND (").append(subSql.toString().trim()).append(")");
        }
        return sql.toString();
    }

    private String createAdvancedWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder("WHERE 1=1 ");

        if (queryParams.containsKey("healthFacilityId") && !StringUtils.isEmpty(queryParams.get("healthFacilityId").get(0))) {
            sql.append("AND ms.healthFacilities.id = :healthFacilityId ");
            values.put("healthFacilityId", Long.parseLong(queryParams.get("healthFacilityId").get(0)));
        }
        if (queryParams.containsKey("status") && !StringUtils.isEmpty(queryParams.get("status").get(0))) {
            sql.append("AND ms.status = :status ");
            values.put("status", Integer.parseInt(queryParams.get("status").get(0)));
        } else {
            sql.append("AND ms.status > :status ");
            values.put("status", Constants.ENTITY_STATUS.DELETED);
        }
        if (queryParams.containsKey("code") && !StringUtils.isEmpty(queryParams.get("code").get(0))) {
            sql.append("AND ms.code LIKE :code ");
            values.put("code", "%" + queryParams.get("code").get(0).trim() + "%");
        }
        if (queryParams.containsKey("name") && !StringUtils.isEmpty(queryParams.get("name").get(0))) {
            sql.append("AND ms.name LIKE :name ");
            values.put("name", "%" + queryParams.get("name").get(0).trim() + "%");
        }
        return sql.toString();
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("ORDER BY ");
        if (queryParams.containsKey("sort")) {
            List<String> orders = queryParams.get("sort");
            for (String i : orders) {
                sql.append("ms." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(", "));
        } else {
            sql.append("ms.createdDate DESC");
        }
        return sql.toString();
    }
}
