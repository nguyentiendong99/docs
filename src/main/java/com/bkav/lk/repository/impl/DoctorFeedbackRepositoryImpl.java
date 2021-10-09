package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.DoctorFeedback;
import com.bkav.lk.repository.custom.DoctorFeedbackRepositoryCustom;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorFeedbackRepositoryImpl implements DoctorFeedbackRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<DoctorFeedback> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        String temp = (queryParams.containsKey("statisticalReport") && StringUtils.isNotBlank(queryParams.getFirst("statisticalReport")))
                ? queryParams.getFirst("statisticalReport").trim() : "";
        boolean isStatisticalReport = Boolean.parseBoolean(temp.trim());
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT F FROM DoctorFeedback F");
        Map<String, Object> values = new HashMap<>();
        if (isStatisticalReport) {
            sql.append(createStatisticalJoinQuery(queryParams));
            sql.append(createStatisticalWhereQuery(queryParams, values));
        } else {
            sql.append(createJoinQuery(queryParams));
            sql.append(createWhereQuery(queryParams, values));
        }
        sql.append(createOrderQuery(queryParams));

        Query query = entityManager.createQuery(sql.toString(), DoctorFeedback.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }


    private String createJoinQuery(MultiValueMap<String, String> queryParams) {
        String sql = " ";
        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " JOIN Doctor DT ON F.doctor.id = DT.id ";
            sql += " JOIN User U ON F.user.id = U.id ";
            sql += " JOIN HealthFacilities H ON F.doctor.healthFacilityId = H.id ";
        }
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))
                ||queryParams.containsKey("doctorFeedback") && !StrUtil.isBlank(queryParams.get("doctorFeedback").get(0))){
            sql += " JOIN Doctor DT ON F.doctor.id = DT.id ";
        }
        if (queryParams.containsKey("feedbackBy") && !StrUtil.isBlank(queryParams.get("feedbackBy").get(0))) {
            sql += " JOIN User U ON F.user.id = U.id ";

        }
            return sql;
    }

    private String createStatisticalJoinQuery(MultiValueMap<String, String> queryParams) {
        String sql = " ";
        sql += " JOIN Doctor DT ON F.doctor.id = DT.id ";
        sql += " JOIN User U ON F.user.id = U.id ";
        sql += " JOIN HealthFacilities H ON F.doctor.healthFacilityId = H.id ";
        // Không còn cột này trong bảng Clinic
//        sql += " JOIN Clinic C ON C.medicalSpecialty.id = F.doctor.medicalSpecialityId ";
        return sql;
    }


    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(F) from DoctorFeedback F ";
        Map<String, Object> values = new HashMap<>();
        sql += (createJoinQuery(queryParams));
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where F.status != 0 ";
        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (lower(F.content) like lower(:keyword) or lower(DT.name) like lower(:keyword) or lower(U.name) like lower(:keyword)" +
                    "or lower(H.name) like lower(:keyword) )";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
                sql += " and  DT.healthFacilityId = :healthFacilityId";
                values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }

        if (queryParams.containsKey("feedbackBy") && !StrUtil.isBlank(queryParams.get("feedbackBy").get(0))) {
            sql += " and lower(U.name) like lower(:feedbackBy)";
            values.put("feedbackBy",'%' + (queryParams.get("feedbackBy").get(0)) + '%');
        }

        if (queryParams.containsKey("doctorFeedback") && !StrUtil.isBlank(queryParams.get("doctorFeedback").get(0))) {
            sql += " and lower(DT.name) like lower(:doctorFeedback)";
            values.put("doctorFeedback",'%' + (queryParams.get("doctorFeedback").get(0)) + '%');
        }

        if (queryParams.containsKey("rate") && !StrUtil.isBlank(queryParams.get("rate").get(0))) {
            sql += " and  F.rate = :rate";
            values.put("rate", Integer.valueOf(queryParams.get("rate").get(0)));
        }
        if (queryParams.containsKey("doctorId") && !StrUtil.isBlank(queryParams.get("doctorId").get(0))) {
            sql += " and F.doctor.id = :doctorId";
            values.put("doctorId", Long.valueOf(queryParams.get("doctorId").get(0)));
        }
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and F.status= :status";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }

        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant startTime;
            startTime = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            if (startTime != null) {
                sql += " and  F.createdDate >= :startDate";
                values.put("startDate", startTime);
            }
        }

        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            Instant endTime;
            endTime = DateUtils.parseEndOfDay(queryParams.get("endDate").get(0));
            if (endTime != null) {
                sql += " and  F.createdDate <= :endDate";
                values.put("endDate", endTime);
            }
        }

        return sql;
    }

    private String createStatisticalWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where 1=1";

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and  DT.healthFacilityId = :healthFacilityId";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }
        if (queryParams.containsKey("clinicId") && !StrUtil.isBlank(queryParams.get("clinicId").get(0))) {
            sql += " and  C.id = :clinicId";
            values.put("clinicId", Long.valueOf(queryParams.get("clinicId").get(0)));
        }
        if (queryParams.containsKey("doctorId") && !StrUtil.isBlank(queryParams.get("doctorId").get(0))) {
            sql += " and F.doctor.id = :doctorId";
            values.put("doctorId", Long.valueOf(queryParams.get("doctorId").get(0)));
        }
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and F.status= :status";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }
        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant startTime;
            startTime = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            if (startTime != null) {
                sql += " and  F.createdDate >= :startDate";
                values.put("startDate", startTime);
            }
        }
        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            Instant endTime;
            endTime = DateUtils.parseEndOfDay(queryParams.get("endDate").get(0));
            if (endTime != null) {
                sql += " and  F.createdDate <= :endDate";
                values.put("endDate", endTime);
            }
        }

        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");
        if (queryParams.containsKey("sort")) {
            List<String> orders = queryParams.get("sort");
            for (String i : orders) {
                sql.append("F.").append(i.replace(",", " ")).append(", ");
            }
            sql.substring(0, sql.lastIndexOf(", "));
        } else {
            if (queryParams.containsKey("status")) {
                sql.append("F.status ASC , ");
            }
            sql.append(" F.createdDate ASC ");
        }

        return sql.toString();
    }
}
