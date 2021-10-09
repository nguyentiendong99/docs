package com.bkav.lk.service;

import com.bkav.lk.dto.MedicationReminderDTO;

import java.util.List;

public interface MedicationReminderService {

    MedicationReminderDTO save(MedicationReminderDTO dto);

    MedicationReminderDTO findById(Long id);

    MedicationReminderDTO findByUserIdAndBookingCodeAndTime(Long userId, String bookingCode, Integer time);

    List<MedicationReminderDTO> findAllByTimeEqual(Integer time);

    List<MedicationReminderDTO> findAllByBookingCode(Long userId, String bookingCode);

    void delete(Long id);
}
