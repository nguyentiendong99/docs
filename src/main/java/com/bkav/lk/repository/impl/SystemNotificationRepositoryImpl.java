package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.SystemNotification;
import com.bkav.lk.repository.custom.SystemNotificationRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemNotificationRepositoryImpl implements SystemNotificationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SystemNotification> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT S FROM SystemNotification S");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), SystemNotification.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "SELECT count(S) FROM SystemNotification S";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    @Override
    public List<SystemNotification> searchForMobile() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        Instant currentDate = DateUtils.parseStartOfDay(date.format(formatter));
        String sql = "SELECT S FROM  SystemNotification S "
                + " WHERE (S.notiStyle = " + Constants.SYS_NOTI_STYLE.ON_DAY
                + " or S.notiStyle = " + Constants.SYS_NOTI_STYLE.FROM_DAY_TO_DAY + ") "
                + " AND S.startDate >= :startDate AND S.status =" + Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED;
        Map<String, Object> values = new HashMap<>();
        values.put("startDate", currentDate);
        Query query = entityManager.createQuery(sql, SystemNotification.class);
        values.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public List<SystemNotification> searchForCronJob(Integer type) {
        List<SystemNotification> list = new ArrayList<>();
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        Instant currentStartDate = DateUtils.parseStartOfDay(date.format(formatter));
        Instant currentEndDate = DateUtils.parseEndOfDay(date.format(formatter));
        Map<String, Object> values = new HashMap<>();
        if(type.equals(Constants.SYS_NOTI_STYLE.ON_DAY)){
            String sql = "SELECT S FROM  SystemNotification S "
                    + " WHERE S.notiStyle = " + Constants.SYS_NOTI_STYLE.ON_DAY
                    + " AND S.startDate = :startDate AND S.status =" + Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED;
            values.put("startDate", currentStartDate);
            Query query = entityManager.createQuery(sql, SystemNotification.class);
            values.forEach(query::setParameter);
            list = query.getResultList();
        }
        if(type.equals(Constants.SYS_NOTI_STYLE.FROM_DAY_TO_DAY)){
            String sql = "SELECT S FROM  SystemNotification S "
                    + " WHERE S.notiStyle = " + Constants.SYS_NOTI_STYLE.FROM_DAY_TO_DAY
                    + " AND S.startDate <= :startDate AND S.endDate >= :endDate "
                    + " AND (S.status =" + Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED + " OR S.status = " + Constants.SYSTEM_NOTIFICATION_STATUS.PUBLISHED +")";
            values.put("startDate", currentStartDate);
            values.put("endDate", currentEndDate);
            Query query = entityManager.createQuery(sql, SystemNotification.class);
            values.forEach(query::setParameter);
            list = query.getResultList();
        }
        return list;
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " WHERE S.status != 0 ";

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (lower(S.code) like lower(:keyword) or lower(S.title) like lower(:keyword)) ";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and  healthFacilityId = :healthFacilityId";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }

        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");
        sql.append(" S.status ASC, S.createdDate DESC ");
        return sql.toString();
    }

}
