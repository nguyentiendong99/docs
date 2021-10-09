package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.SystemNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface SystemNotificationRepositoryCustom {

    List<SystemNotification> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    List<SystemNotification> searchForMobile();

    List<SystemNotification> searchForCronJob(Integer notiType);
}
