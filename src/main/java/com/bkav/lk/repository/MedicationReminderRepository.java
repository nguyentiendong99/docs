package com.bkav.lk.repository;

import com.bkav.lk.domain.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {

    List<MedicationReminder> findAllByTimeAndStatus(Integer time, Integer status);

    List<MedicationReminder> findAllByUserIdAndBookingCode(Long userId, String bookingCode);

    MedicationReminder findByUserIdAndBookingCodeAndTime(Long userId, String bookingCode, Integer time);

}
