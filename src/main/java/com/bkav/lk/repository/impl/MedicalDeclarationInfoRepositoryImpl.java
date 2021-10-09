package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.MedicalDeclarationInfo;
import com.bkav.lk.repository.custom.MedicalDeclarationInfoRepositoryCustom;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalDeclarationInfoRepositoryImpl implements MedicalDeclarationInfoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MedicalDeclarationInfo> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT M FROM MedicalDeclarationInfo M LEFT JOIN DetailMedicalDeclarationInfo D ON M.id = D.medicalDeclarationInfo.id");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), MedicalDeclarationInfo.class);
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
        StringBuilder sql = new StringBuilder();
        sql.append("select count(Distinct M.id) from MedicalDeclarationInfo M LEFT JOIN DetailMedicalDeclarationInfo D ON M.id = D.medicalDeclarationInfo.id ");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        Query query = entityManager.createQuery(sql.toString(), Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    //<editor-fold desc="FUNCTIONS TO CREATE QUERY">
    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = "";
        if (queryParams.containsKey("notManager") && queryParams.get("notManager").get(0).equals("true")) {
            sql += " where M.status != 0 "; // Neu la nguoi dan chi hien nhung ban ghi status = 1 => HOAT DONG
        } else {
            sql += " where M.status in (0,1) "; // Quan ly thi hien ALL
        }

        sql += " and D.medicalDeclarationInfo.id IS NOT NULL ";
        Integer fromYear = null;
        Integer toYear = null;

        if(queryParams.containsKey("keyWord") && !StrUtil.isBlank(queryParams.get("keyWord").get(0))) {
            String keyWord = queryParams.get("keyWord").get(0);

            sql += " and (lower(M.patientRecord.patientRecordCode) like lower(:patientRecordCode) or";
            values.put("patientRecordCode", "%" + keyWord + "%");
            sql += "  lower(M.patientRecord.name) like lower(:name)) ";
            values.put("name", "%" + keyWord + "%");
        }

        if (queryParams.containsKey("gender") && !StrUtil.isBlank(queryParams.get("gender").get(0))) {
            sql += "AND lower(M.patientRecord.gender) LIKE lower(:gender) ";
            values.put("gender", queryParams.get("gender").get(0).trim());
        }
        if (queryParams.containsKey("cityCode") && !StrUtil.isBlank(queryParams.get("cityCode").get(0))) {
            sql += "AND M.patientRecord.city.areaCode LIKE :cityCode ";
            values.put("cityCode", queryParams.get("cityCode").get(0).trim());
        }
        if (queryParams.containsKey("districtCode") && !StrUtil.isBlank(queryParams.get("districtCode").get(0))) {
            sql += "AND M.patientRecord.district.areaCode LIKE :districtCode ";
            values.put("districtCode", queryParams.get("districtCode").get(0).trim());
        }
        if (queryParams.containsKey("wardCode") && !StrUtil.isBlank(queryParams.get("wardCode").get(0))) {
            sql += "AND M.patientRecord.ward.areaCode LIKE :wardCode ";
            values.put("wardCode", queryParams.get("wardCode").get(0).trim());
        }
        if (queryParams.containsKey("ageFrom") && !StrUtil.isBlank(queryParams.get("ageFrom").get(0))) {
            toYear = ZonedDateTime.now().minusYears(Long.parseLong(queryParams.get("ageFrom").get(0).trim())).getYear();
            sql += "AND YEAR(M.patientRecord.dob) <= :toYear ";
            values.put("toYear", toYear);
        }
        if (queryParams.containsKey("ageTo") && !StrUtil.isBlank(queryParams.get("ageTo").get(0))) {
            fromYear = ZonedDateTime.now().minusYears(Long.parseLong(queryParams.get("ageTo").get(0).trim())).getYear();
            sql += "AND YEAR(M.patientRecord.dob) >= :fromYear ";
            values.put("fromYear", fromYear);
        }

        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " AND  M.status = :status";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }

        if (queryParams.containsKey("userId") && !StrUtil.isBlank(queryParams.get("userId").get(0))) {
            sql += " AND M.patientRecord.userId =:userId ";
            values.put("userId", Long.valueOf(queryParams.get("userId").get(0)));
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("group by M.id order by ");
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql.append("M.").append(i.replace(",", " ")).append(", ");
            }
            sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql.append(" M.createdDate desc ");
        }
        return sql.toString();
    }
}
