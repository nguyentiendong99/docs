package com.bkav.lk.repository;

import com.bkav.lk.domain.SystemNotification;
import com.bkav.lk.repository.custom.SystemNotificationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long>, SystemNotificationRepositoryCustom {

    Optional<SystemNotification> findByCodeAndStatusGreaterThan(String code, Integer status);
}
