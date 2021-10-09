package com.bkav.lk.repository.impl;

import com.bkav.lk.repository.custom.DoctorScheduleTimeRepositoryCustom;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorScheduleTimeRepositoryImpl implements DoctorScheduleTimeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Integer totalSUMPeopleRegisteredCustom(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> values = new HashMap<>();
        sql.append("SELECT SUM(d.peopleRegistered) FROM DoctorScheduleTime d ");
        sql.append(createWhereQuery(queryParams, values));
        Query query = entityManager.createQuery(sql.toString());
        values.forEach(query::setParameter);
        if (query.getSingleResult() == null) {
            return null;
        }
        return Integer.valueOf(query.getSingleResult().toString());
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " WHERE d.healthFacilityId = :healthFacilityId AND d.startTime >= :startTime AND d.endTime <= :endTime ";
        Instant startTime = DateUtils.parseToInstant(queryParams.get("startTime").get(0), DateUtils.NORM_2_DATETIME_PATTERN);
        Instant endTime = DateUtils.parseToInstant(queryParams.get("endTime").get(0), DateUtils.NORM_2_DATETIME_PATTERN);
        Long healthFacilityId = Long.valueOf(queryParams.get("healthFacilityId").get(0));

        values.put("startTime", startTime);
        values.put("endTime", endTime);
        values.put("healthFacilityId", healthFacilityId);
        if (queryParams.containsKey("doctorId") && !StrUtil.isBlank(queryParams.get("doctorId").get(0))) {
            sql += " AND d.doctorId in (:doctorIds) ";
            Long doctorId = Long.valueOf(queryParams.get("doctorId").get(0));
            List<Long> doctorIds = Collections.singletonList(doctorId);
            values.put("doctorIds", doctorIds);
        }
        return sql;
    }

}
