package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Notification;
import com.bkav.lk.domain.User;
import com.bkav.lk.repository.UserRepository;
import com.bkav.lk.repository.custom.NotificationRepositoryCustom;
import com.bkav.lk.security.SecurityUtils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Notification> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT N FROM Notification N");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Notification.class);
        values.forEach(query::setParameter);
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "SELECT count(N) FROM Notification N";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " WHERE N.status IN (1, 3) ";

        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql = " WHERE N.status = :status ";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }

        if (queryParams.containsKey("userId") && !StrUtil.isBlank(queryParams.get("userId").get(0))) {
            sql += "AND N.userId = :userId ";
            values.put("userId", Long.valueOf(queryParams.get("userId").get(0)));
        } else {
            sql += "AND N.userId = :userId ";
            String userLogin = SecurityUtils.getCurrentUserLogin().get();
            Optional<User> user = userRepository.findByLoginIgnoreCaseAndStatusIs(userLogin, Constants.ENTITY_STATUS.ACTIVE);
            values.put("userId", user.get().getId());
        }

        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant startDate = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            if (startDate != null) {
                sql += "AND N.lastModifiedDate >= :startDate ";
                values.put("startDate", startDate);
            }
        }

        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");
        sql.append(" N.createdDate DESC ");
        return sql.toString();
    }

}
