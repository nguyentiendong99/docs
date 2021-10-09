package com.bkav.lk.service.impl;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.domain.SystemNotification;
import com.bkav.lk.dto.SystemNotificationDTO;
import com.bkav.lk.dto.SystemNotificationHistoryDTO;
import com.bkav.lk.repository.ActivityLogRepository;
import com.bkav.lk.repository.SystemNotificationRepository;
import com.bkav.lk.service.SystemNotificationService;
import com.bkav.lk.service.mapper.SystemNotificationMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SystemNotificationServiceImpl implements SystemNotificationService {

    private final SystemNotificationRepository systemNotificationRepository;

    private final ActivityLogRepository activityLogRepository;

    private final SystemNotificationMapper systemNotificationMapper;

    private final ObjectMapper objectMapper;

    public SystemNotificationServiceImpl(SystemNotificationRepository systemNotificationRepository, ActivityLogRepository activityLogRepository, SystemNotificationMapper systemNotificationMapper, ObjectMapper objectMapper) {
        this.systemNotificationRepository = systemNotificationRepository;
        this.activityLogRepository = activityLogRepository;
        this.systemNotificationMapper = systemNotificationMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<SystemNotificationDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<SystemNotificationDTO> listDto = systemNotificationMapper.toDto(systemNotificationRepository.search(queryParams, pageable));
        return new PageImpl<>(listDto, pageable, systemNotificationRepository.count(queryParams));
    }

    @Override
    public List<SystemNotificationDTO> searchForMobile() {
        return systemNotificationMapper.toDto(systemNotificationRepository.searchForMobile());
    }

    @Override
    public List<SystemNotification> searchForCronJob(Integer notiType) {
        List<SystemNotification> list = systemNotificationRepository.searchForCronJob(notiType);
        return list;
    }

    @Override
    public SystemNotification findById(Long id) {
        Optional<SystemNotification> item = systemNotificationRepository.findById(id);
        return item.orElse(null);
    }

    @Override
    public SystemNotification save(SystemNotificationDTO systemNotificationDTO) {
        return systemNotificationRepository.save(systemNotificationMapper.toEntity(systemNotificationDTO));
    }

    @Override
    public SystemNotification approve(Long id) {
        Optional<SystemNotification> optional = systemNotificationRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        SystemNotification item = optional.get();
        if (item.getNotiStyle().equals(Constants.SYS_NOTI_STYLE.AFTER_APPROVE)) {
            item.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.PUBLISHED);
        } else {
            item.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED);
        }
        systemNotificationRepository.save(item);
        return item;
    }

    @Override
    public SystemNotification deny(Long id, String rejectReason) {
        Optional<SystemNotification> optional = systemNotificationRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        SystemNotification item = optional.get();
        item.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.DENY);
        item.setRejectReason(rejectReason);
        systemNotificationRepository.save(item);
        return item;
    }

    @Override
    public SystemNotification retrieve(Long id) {
        Optional<SystemNotification> optional = systemNotificationRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        SystemNotification item = optional.get();
        item.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.DEMO);
        systemNotificationRepository.save(item);
        return item;
    }

    @Override
    public SystemNotification delete(Long id) {
        Optional<SystemNotification> optional = systemNotificationRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        SystemNotification item = optional.get();
        item.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.DELETE);
        systemNotificationRepository.save(item);
        return item;
    }

    @Override
    public SystemNotification cancel(Long id) {
        Optional<SystemNotification> optional = systemNotificationRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        SystemNotification item = optional.get();
        item.setStatus(Constants.SYSTEM_NOTIFICATION_STATUS.CANCEL);
        systemNotificationRepository.save(item);
        return item;
    }

    @Override
    public List<SystemNotificationHistoryDTO> getHistory(Long id) {
        List<SystemNotificationHistoryDTO> listHistory = new ArrayList<>();
        List<ActivityLog> listActivityLog = activityLogRepository.findByContentIdAndContentTypeOrderByCreatedDateDesc(id, Constants.CONTENT_TYPE.SYSTEM_NOTIFICATION);
        for (ActivityLog activityLog : listActivityLog) {
            List<String> listContent = new ArrayList<>();
            SystemNotificationHistoryDTO history = new SystemNotificationHistoryDTO();
            history.setCreatedDate(activityLog.getCreatedDate());
            history.setCreatedBy(activityLog.getCreatedBy());
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CREATE)) {
                listContent.add("Thêm mới thông báo");
            } else if (activityLog.getActionType().equals(Constants.ACTION_TYPE.UPDATE)) {
                listContent.add("Chỉnh sửa thông báo");
            } else if (activityLog.getActionType().equals(Constants.ACTION_TYPE.RETRIEVE)) {
                listContent.add("Thu hồi thông báo");
            } else if (activityLog.getActionType().equals(Constants.ACTION_TYPE.APPROVE)) {
                listContent.add("Duyệt thông báo");
            } else if (activityLog.getActionType().equals(Constants.ACTION_TYPE.DENY)) {
                listContent.add("Từ chối duyệt thông báo");
            } else if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CANCEL)) {
                listContent.add("Hủy thông báo");
            }
            history.setContent(listContent);
            listHistory.add(history);
        }
        return listHistory;
    }

    private SystemNotification convertToSystemNotification(String input) {
        if (input.isEmpty()) {
            return null;
        }
        SystemNotification result = null;
        try {
            result = objectMapper.readValue(input, SystemNotification.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            ;
        }
        return result;
    }

    @Override
    public boolean checkUpdateCondition(String currentLogin, SystemNotificationDTO dto) {
        Optional<SystemNotification> optional = systemNotificationRepository.findById(dto.getId());
        if (!optional.isPresent()) {
            return false;
        }
        SystemNotification item = optional.get();
        int status = item.getStatus();
        if (currentLogin.equals(item.getCreatedBy()) &&
                (status == Constants.SYSTEM_NOTIFICATION_STATUS.DEMO
                        || status == Constants.SYSTEM_NOTIFICATION_STATUS.WAITING_APPROVE
                        || status == (Constants.SYSTEM_NOTIFICATION_STATUS.DENY))) {
            return true;
        }
        return currentLogin.equals(dto.getApprovedBy()) &&
                (status == Constants.SYSTEM_NOTIFICATION_STATUS.WAITING_APPROVE
                        || status == Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED
                        || status == Constants.SYSTEM_NOTIFICATION_STATUS.PUBLISHED);
    }

    @Override
    public boolean checkApproveCondition(String currentLogin, SystemNotificationDTO dto) {
        int status = dto.getStatus();
        return currentLogin.equals(dto.getApprovedBy()) &&
                status == Constants.SYSTEM_NOTIFICATION_STATUS.WAITING_APPROVE;
    }

    @Override
    public boolean checkRetrieveCondition(String currentLogin, SystemNotificationDTO dto) {
        int status = dto.getStatus();
        return currentLogin.equals(dto.getCreatedBy()) &&
                status == Constants.SYSTEM_NOTIFICATION_STATUS.WAITING_APPROVE;
    }

    @Override
    public boolean checkCancelCondition(String currentLogin, SystemNotificationDTO dto) {
        int status = dto.getStatus();
        return (currentLogin.equals(dto.getCreatedBy()) || currentLogin.equals(dto.getApprovedBy())) &&
                (status == Constants.SYSTEM_NOTIFICATION_STATUS.APPROVED
                        || status == Constants.SYSTEM_NOTIFICATION_STATUS.DENY
                        || status == Constants.SYSTEM_NOTIFICATION_STATUS.PUBLISHED);
    }

    @Override
    public boolean checkDeleteCondition(String currentLogin, SystemNotificationDTO dto) {
        int status = dto.getStatus();
        return currentLogin.equals(dto.getCreatedBy()) &&
                status == Constants.SYSTEM_NOTIFICATION_STATUS.DEMO;
    }

    @Override
    public String generateSystemNotificationCode(String title) {
        int count = 0;
        String code;
        SystemNotification systemNotification;
        String generateCode = Utils.autoInitializationCodeForDoctor(title.trim());
        while (true) {
            if (count > 0) {
                code = generateCode + String.format("%01d", count);
            } else {
                code = generateCode;
            }
            systemNotification = systemNotificationRepository.findByCodeAndStatusGreaterThan(
                    code, Constants.SYSTEM_NOTIFICATION_STATUS.DELETE).orElse(null);
            if (Objects.isNull(systemNotification)) {
                break;
            }
            count++;
        }
        return code;
    }


}
