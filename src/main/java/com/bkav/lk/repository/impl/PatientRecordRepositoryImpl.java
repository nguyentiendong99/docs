package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.repository.custom.PatientRecordRepositoryCustom;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientRecordRepositoryImpl implements PatientRecordRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PatientRecord> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> values = new HashMap<>();
        String temp = queryParams.get("advancedSearch") != null ? queryParams.get("advancedSearch").get(0) : "";
        String temp2 = (queryParams.containsKey("statisticalReport") && StringUtils.isNotBlank(queryParams.getFirst("statisticalReport")))
                ? queryParams.getFirst("statisticalReport").trim() : "";
        boolean isAdvanced = Boolean.parseBoolean(temp.trim());
        boolean isStatisticalReport = Boolean.parseBoolean(temp2.trim());

        sql.append("SELECT pr FROM PatientRecord pr ");
        if (isAdvanced) {
            sql.append(this.createAdvancedWhereQuery(queryParams, values));
        } else {
            if (isStatisticalReport) {
                sql.append(this.createStatisticalWhereQuery(queryParams, values));
            } else {
                sql.append(this.createWhereQuery(queryParams, values));
            }
        }
        sql.append(this.createOrderQuery(queryParams));

        Query query = entityManager.createQuery(sql.toString(), PatientRecord.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(pr) FROM PatientRecord pr ");
        Map<String, Object> values = new HashMap<>();
        String temp = queryParams.get("advancedSearch") != null ? queryParams.get("advancedSearch").get(0) : "";
        String temp2 = (queryParams.containsKey("statisticalReport") && StringUtils.isNotBlank(queryParams.getFirst("statisticalReport")))
                ? queryParams.getFirst("statisticalReport").trim() : "";
        boolean isAdvanced = Boolean.parseBoolean(temp.trim());
        boolean isStatisticalReport = Boolean.parseBoolean(temp2.trim());

        if (isAdvanced) {
            sql.append(this.createAdvancedWhereQuery(queryParams, values));
        } else {
            if (isStatisticalReport) {
                sql.append(this.createStatisticalWhereQuery(queryParams, values));
            } else {
                sql.append(this.createWhereQuery(queryParams, values));
            }
        }
        Query query = entityManager.createQuery(sql.toString(), Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    @Override
    public List<PatientRecord> getPhoneByPatient(String code, Integer status) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("status", status);
        String sql = "SELECT P " +
                "FROM PatientRecord P " +
                "WHERE P.status = :status " +
                "AND (lower(P.patientRecordCode) like lower(:code)" +
                " OR lower(P.healthInsuranceCode) like lower(:code)" +
                " OR lower(P.phone) like lower(:code)) ";
        Query query = entityManager.createQuery(sql, PatientRecord.class);
        values.forEach(query::setParameter);
        try {
            return (List<PatientRecord>) query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    private String createStatisticalWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder("WHERE 1=1 ");
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql.append(" AND pr.status =:status ");
            values.put("status", Integer.parseInt(queryParams.get("status").get(0)));
        }
        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant fromDate = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            if (fromDate != null) {
                sql.append(" AND pr.createdDate >= :startDate ");
                values.put("startDate", fromDate);
            }
        }
        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            Instant toDate = DateUtils.parseEndOfDay(queryParams.get("endDate").get(0));
            if (toDate != null) {
                sql.append(" AND pr.createdDate <= :endDate ");
                values.put("endDate", toDate);
            }
        }
        return sql.toString();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder("WHERE pr.status = 1 ");
        if (queryParams.containsKey("userId") && !StrUtil.isBlank(queryParams.get("userId").get(0))) {
            sql.append(" AND pr.userId =:userId ");
            values.put("userId", Long.valueOf(queryParams.get("userId").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql.append(" AND (lower(pr.patientRecordCode) LIKE lower(:keyword)  OR lower(pr.phone) LIKE lower(:keyword) OR lower(pr.name) LIKE lower(:keyword)) ");
            values.put("keyword",  "%" + queryParams.get("keyword").get(0).trim() + "%" );
        }

        if (queryParams.containsKey("simpleSearchKeyword") && !StrUtil.isBlank(queryParams.get("simpleSearchKeyword").get(0))) {
            sql.append(" AND (lower(pr.patientRecordCode) LIKE lower(:simpleSearchKeyword)  OR lower(pr.name) LIKE lower(:simpleSearchKeyword)" +
                    " OR lower(pr.healthInsuranceCode) LIKE lower(:simpleSearchKeyword) OR lower(pr.address) LIKE lower(:simpleSearchKeyword)" +
                    " OR lower(pr.phone) LIKE lower(:simpleSearchKeyword) OR lower(pr.email) LIKE lower(:simpleSearchKeyword)) ");
            values.put("simpleSearchKeyword", "%" + queryParams.get("simpleSearchKeyword").get(0).trim() +"%");
        }

        StringBuilder subSql = new StringBuilder();

        if (queryParams.containsKey("patientRecordCode")
                && !StrUtil.isBlank(queryParams.get("patientRecordCode").get(0))) {
            subSql.append("lower(pr.patientRecordCode) LIKE lower(:patientRecordCode) ");
            values.put("patientRecordCode", "%" + queryParams.get("patientRecordCode").get(0) + "%");
        }
        if (queryParams.containsKey("name") && !StrUtil.isBlank(queryParams.get("name").get(0))) {
            subSql.append("OR lower(pr.name) LIKE lower(:name) ");
            values.put("name", "%" + queryParams.get("name").get(0) + "%");
        }
        if (queryParams.containsKey("healthInsuranceCode")
                && !StrUtil.isBlank(queryParams.get("healthInsuranceCode").get(0))) {
            subSql.append("OR lower(pr.healthInsuranceCode) LIKE lower(:healthInsuranceCode) ");
            values.put("healthInsuranceCode", "%" + queryParams.get("healthInsuranceCode").get(0) + "%");
        }
        if (queryParams.containsKey("address") && !StrUtil.isBlank(queryParams.get("address").get(0))) {
            subSql.append("OR lower(pr.address) LIKE lower(:address) ");
            values.put("address", "%" + queryParams.get("address").get(0) + "%");
        }
        if (queryParams.containsKey("phone") && !StrUtil.isBlank(queryParams.get("phone").get(0))) {
            subSql.append("OR pr.phone LIKE :phone ");
            values.put("phone", "%" + queryParams.get("phone").get(0) + "%");
        }
        if (queryParams.containsKey("email") && !StrUtil.isBlank(queryParams.get("email").get(0))) {
            subSql.append("OR lower(pr.email) LIKE lower(:email) ");
            values.put("email", "%" + queryParams.get("email").get(0) + "%");
        }
        if (subSql.toString().length() > 0) {
            sql.append("AND (");
            sql.append(subSql.toString());
            sql.append(") ");
        }
        return sql.toString();
    }

    private String createAdvancedWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        int fromYear;
        int toYear;
        StringBuilder sql = new StringBuilder("WHERE pr.status = 1 ");

        if (queryParams.containsKey("simpleSearchKeyword") && !StrUtil.isBlank(queryParams.get("simpleSearchKeyword").get(0))) {
            sql.append(" AND (lower(pr.patientRecordCode) LIKE lower(:patientRecordCode)  OR lower(pr.name) LIKE lower(:name)) ");
            values.put("patientRecordCode", "%" + queryParams.get("simpleSearchKeyword").get(0).trim() +"%");
            values.put("name", "%" + queryParams.get("simpleSearchKeyword").get(0).trim() +"%" );
        }

        if (queryParams.containsKey("gender") && !StrUtil.isBlank(queryParams.get("gender").get(0))) {
            sql.append("AND lower(pr.gender) LIKE lower(:gender) ");
            values.put("gender", queryParams.get("gender").get(0).trim());
        }
        if (queryParams.containsKey("cityCode") && !StrUtil.isBlank(queryParams.get("cityCode").get(0))) {
            sql.append("AND pr.city.areaCode LIKE :cityCode ");
            values.put("cityCode", queryParams.get("cityCode").get(0).trim());
        }
        if (queryParams.containsKey("districtCode") && !StrUtil.isBlank(queryParams.get("districtCode").get(0))) {
            sql.append("AND pr.district.areaCode LIKE :districtCode ");
            values.put("districtCode", queryParams.get("districtCode").get(0).trim());
        }
        if (queryParams.containsKey("wardCode") && !StrUtil.isBlank(queryParams.get("wardCode").get(0))) {
            sql.append("AND pr.ward.areaCode LIKE :wardCode ");
            values.put("wardCode", queryParams.get("wardCode").get(0).trim());
        }
        if (queryParams.containsKey("ageFrom") && !StrUtil.isBlank(queryParams.get("ageFrom").get(0))) {
            toYear = ZonedDateTime.now().minusYears(Long.parseLong(queryParams.get("ageFrom").get(0).trim())).getYear();
            sql.append("AND YEAR(pr.dob) <= :toYear ");
            values.put("toYear", toYear);
        }
        if (queryParams.containsKey("ageTo") && !StrUtil.isBlank(queryParams.get("ageTo").get(0))) {
            fromYear = ZonedDateTime.now().minusYears(Long.parseLong(queryParams.get("ageTo").get(0).trim())).getYear();
            sql.append("AND YEAR(pr.dob) >= :fromYear ");
            values.put("fromYear", fromYear);
        }
//
//        //for statistical report
//        if (queryParams.containsKey(""))
        return sql.toString();
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("ORDER BY ");
        if (queryParams.containsKey("sort")) {
            List<String> orders = queryParams.get("sort");
            for (String i : orders) {
                sql.append("pr.");
                sql.append(i.replace(",", " "));
                sql.append(", ");
            }
            sql.substring(0, sql.lastIndexOf(", "));
        } else {
            sql.append("pr.createdDate DESC");
        }
        return sql.toString();
    }
}
