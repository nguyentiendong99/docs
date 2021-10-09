package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Feedback;
import com.bkav.lk.repository.custom.FeedbackRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
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

public class FeedbackRepositoryImpl implements FeedbackRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Feedback> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT F FROM Feedback F");
        Map<String, Object> values = new HashMap<>();
        boolean isMobile = false;
        if (queryParams.containsKey("isMobile") && StringUtils.isNotBlank(queryParams.getFirst("isMobile"))) {
            isMobile = Constants.BOOL_NUMBER.TRUE.equals(Integer.parseInt(queryParams.getFirst("isMobile").trim()));
        }
        if (isMobile) {
            sql.append(createJoinQuery(queryParams));
            sql.append(createMobileWhereQuery(queryParams, values));
        } else {
            sql.append(createWhereQuery(queryParams, values));
        }
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Feedback.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(F) from Feedback F ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createJoinQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder();
        sql.append(" JOIN Topic T ON F.topicId = T.id");
        return sql.toString();
    }

    private String createMobileWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " WHERE 1=1";
        if (queryParams.containsKey("userId") && !StrUtil.isBlank(queryParams.get("userId").get(0))) {
            sql += " AND F.userId = :userId";
            values.put("userId", Long.valueOf(queryParams.get("userId").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " AND (F.content LIKE :keyword OR T.name LIKE :keyword)";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }

        return sql;
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where F.status != 0 ";
        if (queryParams.containsKey("userId") && !StrUtil.isBlank(queryParams.get("userId").get(0))) {
            sql += " and  F.userId = :userId";
            values.put("userId", Long.valueOf(queryParams.get("userId").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and F.content like :keyword";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }

        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and  F.status IN (:status)";
            String[] stringList = queryParams.get("status").get(0).split(",");
            List<Integer> statusList = new ArrayList<>();
            for (String str : stringList) {
                statusList.add(Integer.valueOf(str));
            }
            values.put("status", statusList);
        }

        if (queryParams.containsKey("startTime") && !StrUtil.isBlank(queryParams.get("startTime").get(0))) {
            Instant startTime;
            startTime = DateUtils.parseStartOfDay(queryParams.get("startTime").get(0));
            if (startTime != null) {
                sql += " and  F.createdDate >= :startTime";
                values.put("startTime", startTime);
            }
        }

        if (queryParams.containsKey("endTime") && !StrUtil.isBlank(queryParams.get("endTime").get(0))) {
            Instant endTime;
            endTime = DateUtils.parseEndOfDay(queryParams.get("endTime").get(0));
            if (endTime != null) {
                sql += " and  F.createdDate <= :endTime";
                values.put("endTime", endTime);
            }
        }

        if (queryParams.containsKey("topicId") && !StrUtil.isBlank(queryParams.get("topicId").get(0))) {
            sql += " and  F.topicId = :topicId";
            values.put("topicId", Long.valueOf(queryParams.get("topicId").get(0)));
        }

        if (queryParams.containsKey("feedbackedUnitId") && !StrUtil.isBlank(queryParams.get("feedbackedUnitId").get(0))) {
            sql += " and  F.feedbackedUnit.id = :feedbackedUnitId";
            values.put("feedbackedUnitId", Long.valueOf(queryParams.get("feedbackedUnitId").get(0)));
        }

        if (queryParams.containsKey("processingUnitId") && !StrUtil.isBlank(queryParams.get("processingUnitId").get(0))) {
            sql += " and  F.processingUnit.id = :processingUnitId";
            values.put("processingUnitId", Long.valueOf(queryParams.get("processingUnitId").get(0)));
        }

        if (queryParams.containsKey("processedBy") && !StrUtil.isBlank(queryParams.get("processedBy").get(0))) {
            sql += " and F.processedBy = :processedBy";
            values.put("processedBy", queryParams.get("processedBy").get(0));
        }

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and  F.feedbackedUnit.id = :healthFacilityId";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
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
        }
        if (queryParams.containsKey("isMobile") && StringUtils.isNotBlank(queryParams.getFirst("isMobile"))) {
            boolean isMobile = Constants.BOOL_NUMBER.TRUE.equals(Integer.parseInt(queryParams.getFirst("isMobile").trim()));
            if (isMobile) {
                sql.append("F.status ASC, F.createdDate DESC");
            }
        } else {
            sql.append("F.status ASC, F.createdDate ASC");
        }
        return sql.toString();
    }
}
