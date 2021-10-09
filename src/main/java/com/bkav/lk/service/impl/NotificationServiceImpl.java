package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Notification;
import com.bkav.lk.dto.DeviceDTO;
import com.bkav.lk.dto.NotificationDTO;
import com.bkav.lk.repository.NotificationRepository;
import com.bkav.lk.service.DeviceService;
import com.bkav.lk.service.NotificationService;
import com.bkav.lk.service.mapper.NotificationMapper;
import com.bkav.lk.service.notification.firebase.FCMNotificationRequest;
import com.bkav.lk.service.notification.firebase.FCMService;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ObjectMapper objectMapper;

    private final NotificationRepository repository;

    private final NotificationMapper mapper;

    private final FCMService fcmService;

    private final DeviceService deviceService;

    public NotificationServiceImpl(NotificationRepository repository,
                                   ObjectMapper objectMapper,
                                   NotificationMapper mapper,
                                   FCMService fcmService,
                                   DeviceService deviceService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
        this.fcmService = fcmService;
        this.deviceService = deviceService;
    }

    @Override
    public Page<NotificationDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<NotificationDTO> notificationDTOS = mapper.toDto(repository.search(queryParams, pageable));
        return new PageImpl<>(notificationDTOS, pageable, repository.count(queryParams));
    }

    @Override
    public NotificationDTO findById(Long id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public Long findAllByNotificationUnRead(MultiValueMap<String, String> queryParams) {
        Long count = repository.count(queryParams);
        if (count == null) {
            count = 0L;
        }
        return count;
    }

    @Override
    public NotificationDTO save(NotificationDTO notificationDTO) {
        Notification notification = mapper.toEntity(notificationDTO);
        notification = repository.save(notification);
        return mapper.toDto(notification);
    }

    @Override
    public void pushNotification(String template,
                                 FirebaseData data,
                                 List<String> paramTitle, List<String> paramBody, Long userId) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle(StrUtil.getStringFromBundle(template + ".title", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, paramTitle));
        fcmNotificationRequest.setBody(StrUtil.getStringFromBundle(template + ".body", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, paramBody));
        data.setBody(fcmNotificationRequest.getBody());
        data.setTitle(fcmNotificationRequest.getTitle());
        fcmNotificationRequest.setData(objectMapper.convertValue(data, Map.class));
        List<DeviceDTO> devicesDTO = deviceService.findByUserId(userId);
        for (DeviceDTO deviceDTO : devicesDTO) {
            fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
            fcmService.sendToDevice(fcmNotificationRequest);
        }
        NotificationDTO notificationDTO = createNotification(fcmNotificationRequest, data, userId);
        repository.save(mapper.toEntity(notificationDTO));
    }

    @Override
    public void saveNotification(String template, FirebaseData data, List<String> paramTitle, List<String> paramBody, Long userId) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle(StrUtil.getStringFromBundle(template + ".title", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, paramTitle));
        fcmNotificationRequest.setBody(StrUtil.getStringFromBundle(template + ".body", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, paramBody));
        data.setBody(fcmNotificationRequest.getBody());
        data.setTitle(fcmNotificationRequest.getTitle());
        fcmNotificationRequest.setData(objectMapper.convertValue(data, Map.class));
        NotificationDTO notificationDTO = createNotification(fcmNotificationRequest, data, userId);
        repository.save(mapper.toEntity(notificationDTO));
    }

    @Override
    public void pushNotificationNoTemplate(FirebaseData data, String title, String body, List<Long> userIdList) {
        FCMNotificationRequest fcmNotificationRequest = new FCMNotificationRequest();
        fcmNotificationRequest.setTitle(title);
        data.setTitle(title);
        data.setBody(body);
        fcmNotificationRequest.setData(objectMapper.convertValue(data, Map.class));
        fcmNotificationRequest.setBody(body);
        List<DeviceDTO> devicesDTO = deviceService.findByUserIdIn(userIdList);
        for (DeviceDTO deviceDTO : devicesDTO) {
            fcmNotificationRequest.setToken(deviceDTO.getFirebaseToken());
            fcmService.sendToDevice(fcmNotificationRequest);
        }
        List<NotificationDTO> list = new ArrayList<>();
        for (Long userId : userIdList) {
            NotificationDTO notificationDTO = createNotification(fcmNotificationRequest, data, userId);
            list.add(notificationDTO);
        }
        repository.saveAll(mapper.toEntity(list));
    }

    private NotificationDTO createNotification(FCMNotificationRequest fcmNotificationRequest, FirebaseData data, Long userId) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(fcmNotificationRequest.getTitle());
        notificationDTO.setBody(fcmNotificationRequest.getBody());
        notificationDTO.setType(data.getType());
        notificationDTO.setUserId(userId);
        notificationDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
        if (data.getObject() != null) {
            try {
                notificationDTO.setObject(objectMapper.writeValueAsString(data.getObject()));
            } catch (JsonProcessingException e) {
                notificationDTO.setObject(null);
                log.error("covert object to string json failed");
            }
        }
        return notificationDTO;
    }
}
