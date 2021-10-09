package com.bkav.lk.repository;

import com.bkav.lk.domain.Notification;
import com.bkav.lk.repository.custom.NotificationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {


}
