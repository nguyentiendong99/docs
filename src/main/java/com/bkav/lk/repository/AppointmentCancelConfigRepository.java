package com.bkav.lk.repository;

import com.bkav.lk.domain.AppointmentCancelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentCancelConfigRepository extends JpaRepository<AppointmentCancelLog, Long> {

    List<AppointmentCancelLog> findByUserId(Long userId);

    Boolean existsByUserIdAndIsBlocked(Long userId, Integer status);
}
