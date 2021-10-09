package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.repository.custom.DoctorScheduleRepositoryCustom;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bkav.lk.util.StrUtil.convertList;

public class DoctorScheduleRepositoryImpl implements DoctorScheduleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<DoctorSchedule> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT D FROM DoctorSchedule D");
        Map<String, Object> values = new HashMap<>();
        sql.append(createJoinQuery(queryParams));
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), DoctorSchedule.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(DISTINCT D) from DoctorSchedule D";
        Map<String, Object> values = new HashMap<>();
        sql += createJoinQuery(queryParams);
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createJoinQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder();
        sql.append(" JOIN Doctor DT ON D.doctor.id = DT.id ");
        sql.append(" JOIN MedicalSpeciality MS ON DT.medicalSpeciality.id = MS.id ");
        sql.append(" LEFT JOIN Clinic C ON D.clinic.id = C.id ");
        return sql.toString();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where DT.status = 1 AND MS.status = 1 ";
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " AND D.status = :status ";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        } else {
            sql += " AND D.status = 1 ";
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " AND (lower(DT.code) like lower(:keyword) OR lower(DT.name) like lower(:keyword) OR lower(C.name) LIKE lower(:keyword)) ";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }

        if (queryParams.containsKey("fromDate") && !StrUtil.isBlank(queryParams.get("fromDate").get(0))) {
            Instant fromDate = DateUtils.parseStartOfDay(queryParams.get("fromDate").get(0));
            if (fromDate != null) {
                sql += " AND D.workingDate >= :fromDate ";
                values.put("fromDate", fromDate);
            }
        } else {
            sql += " AND D.workingDate >= :fromDate ";
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime startTime = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
            values.put("fromDate", startTime.toInstant());
        }

        if (queryParams.containsKey("toDate") && !StrUtil.isBlank(queryParams.get("toDate").get(0))) {
            Instant toDate = DateUtils.parseEndOfDay(queryParams.get("toDate").get(0));
            if (toDate != null) {
                sql += " AND D.workingDate <= :toDate ";
                values.put("toDate", toDate);
            }
        }

        if (queryParams.containsKey("workingTime") && !StrUtil.isBlank(queryParams.get("workingTime").get(0))) {
            sql += " AND D.workingTime = :workingTime ";
            values.put("workingTime", Integer.valueOf(queryParams.get("workingTime").get(0)));
        }

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and  DT.healthFacilityId = :healthFacilityId";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }

        // Không còn cột này trong bảng clinic
//        if (queryParams.containsKey("medicalSpecialtyId") && !StrUtil.isBlank(queryParams.get("medicalSpecialtyId").get(0))) {
//            sql += " AND C.medicalSpecialty.id = :medicalSpecialtyId ";
//            values.put("medicalSpecialtyId", Long.valueOf(queryParams.get("medicalSpecialtyId").get(0)));
//        }

        if (queryParams.containsKey("clinicId") && !StrUtil.isBlank(queryParams.get("clinicId").get(0))) {
            sql += " AND C.id = :clinicId ";
            values.put("clinicId", Long.valueOf(queryParams.get("clinicId").get(0)));
        }
        if (queryParams.containsKey("doctorId") && StringUtils.isNotBlank(queryParams.getFirst("doctorId"))) {
            sql += " AND D.doctor.id = :doctorId ";
            values.put("doctorId", Long.valueOf(queryParams.getFirst("doctorId")));
        }
        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0) {
                values.put("ids", list);
                sql += " AND D.id IN (:ids)";
            }
        }
        return sql;
    }
    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" order by ");
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql.append("D." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql.append(" D.workingDate asc ");
        }
        return sql.toString();
    }

    @Override
    public List<DoctorSchedule> findAllByTimeSelected(Long healthFacilityId, Instant workingDate) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> values = new HashMap<>();
        sql.append("SELECT DS " +
                "FROM DoctorSchedule DS " +
                "WHERE DS.status = 1 AND " +
                "DS.doctor.id IN (SELECT D.id FROM Doctor D WHERE D.status = 1 AND D.healthFacilityId = :healthFacilityId) " +
                "AND DS.workingDate = :workingDate");
        values.put("workingDate", workingDate);
        values.put("healthFacilityId", healthFacilityId);
        Query query = entityManager.createQuery(sql.toString(), DoctorSchedule.class);
        values.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public List<Instant> findAllAvailableInHospital(Long healthFacilityId, Instant startDate, Instant endDate) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> values = new HashMap<>();
        sql.append("SELECT ds.workingDate " +
                "FROM DoctorSchedule ds " +
                "WHERE ds.status = 1 AND ds.doctor.id IN (SELECT d.id FROM Doctor d WHERE d.status = 1 AND d.healthFacilityId = :healthFacilityId) " +
                "AND ds.workingDate >= :startDate AND ds.workingDate <= :endDate " +
                "GROUP BY ds.workingDate " +
                "ORDER BY ds.workingDate");
        values.put("healthFacilityId", healthFacilityId);
        values.put("startDate", startDate);
        values.put("endDate", endDate);
        Query query = entityManager.createQuery(sql.toString());
        values.forEach(query::setParameter);
        return (List<Instant>) query.getResultList();
    }
}
