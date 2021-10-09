package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Subclinical;
import com.bkav.lk.repository.custom.SubclinicalRepositoryCustom;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bkav.lk.util.StrUtil.convertList;

public class SubclinicalRepositoryImpl implements SubclinicalRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Subclinical> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT S FROM Subclinical S");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Subclinical.class);
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
        sql.append("SELECT count(S) FROM Subclinical S");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        Query query = entityManager.createQuery(sql.toString(), Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " LEFT JOIN DoctorAppointment D ON D.appointmentCode = S.doctorAppointmentCode";
        sql += " LEFT JOIN PatientRecord P ON P.id = D.patientRecord.id ";
        sql += " WHERE 1 = 1";
        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and  D.healthFacilityId = :healthFacilityId";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (S.doctorAppointmentCode like :keyword " +
                    "or P.patientRecordCode like :keyword " +
                    "or P.name like :keyword )";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }

        if(queryParams.containsKey("cls_madichvu") && !StrUtil.isBlank(queryParams.get("cls_madichvu").get(0))){
            sql += " and  lower(S.code) = lower(:cls_madichvu)";
            values.put("cls_madichvu", queryParams.get("cls_madichvu").get(0)  );
        }


        if(queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))){
            sql += " and  S.status = :status";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }

        if(queryParams.containsKey("cls_kithuatvien") && !StrUtil.isBlank(queryParams.get("cls_kithuatvien").get(0))){
            sql += " and  lower(S.technician) like lower(:cls_kithuatvien)";
            values.put("cls_kithuatvien", "%" + queryParams.get("cls_kithuatvien").get(0) + "%");
        }

        if(queryParams.containsKey("cls_phongthuchien") && !StrUtil.isBlank(queryParams.get("cls_phongthuchien").get(0))){
            sql += " and  lower(S.room) like lower(:cls_phongthuchien)";
            values.put("cls_phongthuchien", "%" +  queryParams.get("cls_phongthuchien").get(0) + "%");
        }


        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant startDate;
            startDate = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            if (startDate != null) {
                sql += " and  D.startTime >= :startDate";
                values.put("startDate", startDate);
            }
        }

        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            Instant endDate;
            endDate = DateUtils.parseEndOfDay(queryParams.get("endDate").get(0));
            if (endDate != null) {
                sql += " and  D.endTime <= :endDate";
                values.put("endDate", endDate);
            }
        }

        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0) {
                values.put("ids", list);
                sql += " AND S.id IN (:ids)";
            }
        }

        return sql;
    }
    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");
        if (queryParams.containsKey("sort")) {
            List<String> orders = queryParams.get("sort");
            for (String i : orders) {
                sql.append("S." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(", "));
        } else {
            sql.append(" S.status ASC , S.createdDate DESC");
        }
        return sql.toString();
    }
}
