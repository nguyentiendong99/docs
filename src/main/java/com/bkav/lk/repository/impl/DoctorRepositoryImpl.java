package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Doctor;
import com.bkav.lk.repository.custom.DoctorRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.bkav.lk.util.StrUtil.convertList;

public class DoctorRepositoryImpl implements DoctorRepositoryCustom {

    private static final String ENTITY_NAME = "doctor";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Doctor> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> values = new HashMap<>();
//        String temp = queryParams.containsKey("advancedSearch") ? queryParams.get("advancedSearch").get(0) : "false";
//        boolean isAdvancedSearch = Boolean.parseBoolean(temp);

        sql.append("SELECT DISTINCT d FROM Doctor d ");
        sql.append("INNER JOIN Academic a ON a.id = d.academic.id ");
//        if (isAdvancedSearch) {
//            sql.append(createAdvancedWhereQuery(queryParams, values));
//        } else {
//            sql.append(createWhereQuery(queryParams, values));
//        }
        sql.append(createAdvancedWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));

        Query query = entityManager.createQuery(sql.toString(), Doctor.class);
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(d) FROM Doctor d ");
        sql.append("INNER JOIN Academic a ON a.id = d.academic.id ");
        Map<String, Object> values = new HashMap<>();
//        String temp = queryParams.containsKey("advancedSearch") ? queryParams.get("advancedSearch").get(0) : "false";
//        boolean isAdvancedSearch = Boolean.parseBoolean(temp);

//        if (isAdvancedSearch) {
//            sql.append(createAdvancedWhereQuery(queryParams, values));
//        } else {
//            sql.append(createWhereQuery(queryParams, values));
//        }
        sql.append(createAdvancedWhereQuery(queryParams, values));
        Query query = entityManager.createQuery(sql.toString(), Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    @Override
    public List<Doctor> findByMainHealthFacilityIdAndStatus(Long parentId, Integer status) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> values = new HashMap<>();

        sql.append("SELECT d FROM Doctor d INNER JOIN HealthFacilities hf ON d.healthFacilityId = hf.id" +
                " WHERE d.status = :status");
        values.put("status", status);
        Query query = entityManager.createQuery(sql.toString(), Doctor.class);
        values.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public List<Doctor> findAllDoctorsWithin30Days(MultiValueMap<String, String> queryParams, Pageable pageable) {
        String sql = "SELECT d.*" +
                    " FROM doctor d" +
                    " JOIN (SELECT DISTINCT(ds.doctor_id)" +
                          " FROM doctor_schedule ds" +
                          " WHERE ds.status = 1 AND ds.doctor_id IN (SELECT d.id" +
                                                                   " FROM doctor d" +
                                                                   " WHERE d.health_facility_id = :healthFacilityId AND d.status = 1)" +
                          " AND ds.working_date >= :workingDate AND ds.working_date <= :workingDateMax" +
                          " GROUP BY ds.doctor_id) as doctors ON d.id = doctors.doctor_id WHERE 1=1 ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQueryDoctorsWithin30Days(values, queryParams);
        Query query = entityManager.createNativeQuery(sql, Doctor.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    public String createWhereQueryDoctorsWithin30Days(Map<String, Object> values, MultiValueMap<String, String> queryParams) {
        String sql = "";
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }
        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " AND lower(d.name) LIKE lower(:keyword)";
            values.put("keyword", "%" + queryParams.get("keyword").get(0).trim() + "%");
        }
        if (queryParams.containsKey("academicId") && !StrUtil.isBlank(queryParams.get("academicId").get(0))) {
            sql += " AND d.academic_id = :academicId ";
            values.put("academicId", Long.valueOf(queryParams.get("academicId").get(0)));
        }
        if (queryParams.containsKey("medicalSpecialityId") && !StrUtil.isBlank(queryParams.get("medicalSpecialityId").get(0))) {
            sql += " AND d.medical_speciality_id = :medicalSpecialityId ";
            values.put("medicalSpecialityId", Long.valueOf(queryParams.get("medicalSpecialityId").get(0)));
        }
        if (queryParams.containsKey("gender") && !StrUtil.isBlank(queryParams.get("gender").get(0))) {
            String gender = queryParams.get("gender").get(0).trim();
            sql += " AND lower(d.gender) LIKE lower(:gender)";
            values.put("gender", gender);
        }
        ZonedDateTime now = ZonedDateTime.of(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh"));
        values.put("workingDate", now.plusDays(1).toInstant());
        values.put("workingDateMax", now.plusDays(30).toInstant());
        return sql;
    }

    @Override
    public Long countDoctorsWithin30Days(MultiValueMap<String, String> queryParams) {
        String sql = "SELECT COUNT(1)" +
                    " FROM doctor d" +
                    " JOIN (SELECT DISTINCT(ds.doctor_id)" +
                          " FROM doctor_schedule ds" +
                          " WHERE ds.status = 1 AND ds.doctor_id IN (SELECT d.id" +
                                                                   " FROM doctor d" +
                                                                   " WHERE d.health_facility_id = :healthFacilityId AND d.status = 1)" +
                          " AND ds.working_date >= :workingDate AND ds.working_date <= :workingDateMax" +
                          " GROUP BY ds.doctor_id) as doctors ON d.id = doctors.doctor_id WHERE 1=1 ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQueryDoctorsWithin30Days(values, queryParams);
        Query query = entityManager.createNativeQuery(sql);
        values.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = "WHERE d.status > 0 ";
        String subSql = "";
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " AND d.status = :status ";
            values.put("status", Integer.parseInt(queryParams.get("status").get(0)));
        }
        if (queryParams.containsKey("medicalSpecialityId") && !StrUtil.isBlank(queryParams.get("medicalSpecialityId").get(0))) {
            sql += " AND d.medicalSpeciality.id = :medicalSpecialityId ";
            values.put("medicalSpecialityId", Long.parseLong(queryParams.get("medicalSpecialityId").get(0)));
        }
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " AND d.healthFacilityId = :healthFacilityId ";
            values.put("healthFacilityId", Long.parseLong(queryParams.get("healthFacilityId").get(0)));
        }

        if (queryParams.containsKey("code") && !StrUtil.isBlank(queryParams.get("code").get(0))) {
            subSql += " OR lower(d.code) LIKE lower(:code)";
            values.put("code", "%" + queryParams.get("code").get(0).trim() + "%");
        }
        if (queryParams.containsKey("name") && !StrUtil.isBlank(queryParams.get("name").get(0))) {
            subSql += " OR lower(d.name) LIKE lower(:name) ";
            values.put("name", "%" + queryParams.get("name").get(0).trim() + "%");
        }
        if (queryParams.containsKey("academicName") && !StrUtil.isBlank(queryParams.get("academicName").get(0))) {
            subSql += " OR lower(d.academic.name) LIKE lower(:academicName) ";
            values.put("academicName", "%" + queryParams.get("academicName").get(0).trim() + "%");
        }
        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0) {
                values.put("ids", list);
                sql += " AND d.id IN (:ids)";
            }
        }
        if (subSql.length() > 0) {
            sql += "AND (" + subSql.replaceFirst("OR", "") + ") ";
        }
        return sql;
    }

    private String createAdvancedWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = "WHERE d.status > 0 ";

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " AND (lower(a.name) LIKE lower(:keyword) OR lower(d.code) LIKE lower(:keyword) OR lower(d.name) LIKE lower(:keyword) )";
            values.put("keyword", "%" + queryParams.get("keyword").get(0).trim() + "%");
        }
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " AND d.status = :status ";
            values.put("status", Integer.parseInt(queryParams.get("status").get(0)));
        }
        if (queryParams.containsKey("medicalSpecialityId")
                && !StrUtil.isBlank(queryParams.get("medicalSpecialityId").get(0))) {
            sql += " AND d.medicalSpeciality.id = :medicalSpecialityId ";
            values.put("medicalSpecialityId", Long.parseLong(queryParams.get("medicalSpecialityId").get(0)));
        }
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " AND d.healthFacilityId = :healthFacilityId ";
            values.put("healthFacilityId", Long.parseLong(queryParams.get("healthFacilityId").get(0)));
        }
        if (queryParams.containsKey("positionId")
                && !StrUtil.isBlank(queryParams.get("positionId").get(0))) {
            sql += " AND d.positionId = :positionId ";
            values.put("positionId", Long.parseLong(queryParams.get("positionId").get(0)));
        }
        if (queryParams.containsKey("academicId")
                && !StrUtil.isBlank(queryParams.get("academicId").get(0))) {
            sql += " AND d.academic.id = :academicId ";
            values.put("academicId", Long.parseLong(queryParams.get("academicId").get(0).trim()));
        }

        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0) {
                values.put("ids", list);
                sql += " AND d.id IN (:ids)";
            }
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");
        if (queryParams.containsKey("sort")) {
            List<String> orders = queryParams.get("sort");
            for (String i : orders) {
                sql.append("d." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(", "));
        } else {
            sql.append("d.createdDate DESC");
        }
        return sql.toString();
    }
}
