package com.bkav.lk.service;

import com.bkav.lk.domain.AbstractAuditingEntity;
import com.bkav.lk.domain.ActivityLog;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface ActivityLogService {

    List<ActivityLog> search(MultiValueMap<String, String> queryParams);

    void create(Integer contentType, AbstractAuditingEntity content);

    void update(Integer contentType, AbstractAuditingEntity oldContet, AbstractAuditingEntity newContent);

    void delete(Integer contentType, AbstractAuditingEntity content);

    void multipleCreate(Integer contentType, List<AbstractAuditingEntity> contents);

    void multipleDelete(Integer contentType, List<AbstractAuditingEntity> contents);

    void approve(Integer contentType, AbstractAuditingEntity content);

    void deny(Integer contentType, AbstractAuditingEntity content);

    void confirm(Integer contentType, AbstractAuditingEntity content);

    void cancel(Integer contentType, AbstractAuditingEntity content);

    void waiting(Integer contentType, AbstractAuditingEntity content);

    void processing(Integer contentType, AbstractAuditingEntity content);

    void done(Integer contentType, AbstractAuditingEntity content);

    void retrieve(Integer contentType, AbstractAuditingEntity content);
}
