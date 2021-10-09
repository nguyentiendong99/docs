package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.CategoryConfigIcon;
import com.bkav.lk.repository.custom.CategoryConfigIconRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryConfigIconRepositoryImpl implements CategoryConfigIconRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CategoryConfigIcon> search(MultiValueMap<String, String> queryParams, Long healthFacilityId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT C FROM CategoryConfigIcon C ");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values, healthFacilityId));
        Query query = entityManager.createQuery(sql.toString(), CategoryConfigIcon.class);
        values.forEach(query::setParameter);
        return query.getResultList();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values, Long healthFacilityId) {
        String sql = " where C.healthFacilities.id = :healthFacilityId ";
        values.put("healthFacilityId",healthFacilityId);
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and  C.status IN (:status)";
            String[] stringList = queryParams.get("status").get(0).split(",");
            List<Integer> statusList = new ArrayList<>();
            for (String str : stringList) {
                statusList.add(Integer.valueOf(str));
            }
            values.put("status", statusList);
        }
        if (queryParams.containsKey("configType") && !StrUtil.isBlank(queryParams.get("configType").get(0))) {
            sql += " and  C.type = :type";
            values.put("type", queryParams.get("configType").get(0));
        }
        if (queryParams.containsKey("display") && !StrUtil.isBlank(queryParams.get("display").get(0))) {
            sql += " and  C.display = :display";
            values.put("display", Integer.valueOf(queryParams.get("display").get(0)));
        }
        return sql;
    }
}
