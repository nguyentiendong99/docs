package com.bkav.lk.service.impl;

import com.bkav.lk.domain.AbstractAuditingEntity;
import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.domain.User;
import com.bkav.lk.repository.ActivityLogRepository;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    //<editor-fold desc="INIT">
    public final ActivityLogRepository activityLogRepository;

    public final UserService userService;

    //    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository, UserService userService) {
        this.activityLogRepository = activityLogRepository;
        this.userService = userService;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    //</editor-fold>

    @Override
    public List<ActivityLog> search(MultiValueMap<String, String> queryParams) {
        return activityLogRepository.search(queryParams);
    }

    @Override
    public void create(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.CREATE);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void update(Integer contentType, AbstractAuditingEntity oldContent, AbstractAuditingEntity newContent) {
        if (oldContent == null || newContent == null) return;
        ActivityLog activityLog = createActivityLog(contentType, newContent);
        activityLog.setActionType(Constants.ACTION_TYPE.UPDATE);
        activityLog.setUserId(getUserId());
        try {
            activityLog.setOldContent(objectMapper.writeValueAsString(oldContent));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        activityLogRepository.save(activityLog);
    }

    @Override
    public void delete(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.DELETE);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void multipleCreate(Integer contentType, List<AbstractAuditingEntity> contents) {
        if (contents.isEmpty()) return;
        List<ActivityLog> activityLogs = new ArrayList<>();
        ActivityLog activityLog = null;
        for (AbstractAuditingEntity content : contents) {
            activityLog = createActivityLog(contentType, content);
            activityLog.setActionType(Constants.ACTION_TYPE.CREATE);
            activityLog.setUserId(getUserId());
            activityLogs.add(activityLog);
        }
        activityLogRepository.saveAll(activityLogs);
    }

    @Override
    public void multipleDelete(Integer contentType, List<AbstractAuditingEntity> contents) {
        if (contents.isEmpty()) return;
        List<ActivityLog> activityLogs = new ArrayList<>();
        ActivityLog activityLog = null;
        for (AbstractAuditingEntity content : contents) {
            activityLog = createActivityLog(contentType, content);
            activityLog.setActionType(Constants.ACTION_TYPE.DELETE);
            activityLog.setUserId(getUserId());
            activityLogs.add(activityLog);
        }
        activityLogRepository.saveAll(activityLogs);
    }


    @Override
    public void approve (Integer contentType, AbstractAuditingEntity content){
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.APPROVE);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void deny (Integer contentType, AbstractAuditingEntity content){
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.DENY);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void confirm(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.CONFIRM);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void cancel(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.CANCEL);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    private ActivityLog createActivityLog(Integer contentType, AbstractAuditingEntity content){
        ActivityLog activityLog = new ActivityLog();
        activityLog.setContentType(contentType);
        activityLog.setContentId(content.getId());
        activityLog.setUserId(getUserId());
        try {
            activityLog.setContent(objectMapper.writeValueAsString(content));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return activityLog;
    }

    private Long getUserId() {
        Optional<User> user = userService.getUserWithAuthorities();
        return user.map(User::getId).orElse(null);
    }

    @Override
    public void waiting(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.WAITING);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void processing(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.PROCESSING);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void done(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.DONE);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }

    @Override
    public void retrieve(Integer contentType, AbstractAuditingEntity content) {
        if (content == null) return;
        ActivityLog activityLog = createActivityLog(contentType, content);
        activityLog.setActionType(Constants.ACTION_TYPE.RETRIEVE);
        activityLog.setUserId(getUserId());
        activityLogRepository.save(activityLog);
    }
}
