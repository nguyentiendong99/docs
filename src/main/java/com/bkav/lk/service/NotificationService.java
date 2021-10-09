package com.bkav.lk.service;

import com.bkav.lk.dto.NotificationDTO;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface NotificationService {

    Page<NotificationDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    NotificationDTO findById(Long id);

    Long findAllByNotificationUnRead(MultiValueMap<String, String> queryParams);

    NotificationDTO save(NotificationDTO notificationDTO);

    void saveNotification(String template, FirebaseData data, List<String> paramTitle, List<String> paramBody, Long userId);

    void pushNotification(String template, FirebaseData data, List<String> paramTitle, List<String> paramBody, Long userId);

    void pushNotificationNoTemplate(FirebaseData data, String title, String body, List<Long> userIdList);
}
