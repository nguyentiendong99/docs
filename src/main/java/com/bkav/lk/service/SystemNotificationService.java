package com.bkav.lk.service;

import com.bkav.lk.domain.SystemNotification;
import com.bkav.lk.dto.SystemNotificationDTO;
import com.bkav.lk.dto.SystemNotificationHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface SystemNotificationService {

    Page<SystemNotificationDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    List<SystemNotificationDTO> searchForMobile();

    List<SystemNotification> searchForCronJob(Integer type);

    SystemNotification findById(Long id);

    SystemNotification save(SystemNotificationDTO systemNotificationDTO);

    SystemNotification approve(Long id);

    SystemNotification deny(Long id, String rejectReason);

    SystemNotification retrieve(Long id);

    SystemNotification delete(Long id);

    SystemNotification cancel(Long id);

    List<SystemNotificationHistoryDTO> getHistory(Long id);

    boolean checkUpdateCondition(String currentLogin, SystemNotificationDTO dto);

    boolean checkApproveCondition(String currentLogin, SystemNotificationDTO dto);

    boolean checkRetrieveCondition(String currentLogin, SystemNotificationDTO dto);

    boolean checkCancelCondition(String currentLogin, SystemNotificationDTO dto);

    boolean checkDeleteCondition(String currentLogin, SystemNotificationDTO dto);

    String generateSystemNotificationCode(String title);
}
