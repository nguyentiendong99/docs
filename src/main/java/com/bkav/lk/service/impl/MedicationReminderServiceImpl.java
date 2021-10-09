package com.bkav.lk.service.impl;

import com.bkav.lk.domain.MedicationReminder;
import com.bkav.lk.dto.MedicationReminderDTO;
import com.bkav.lk.repository.MedicationReminderRepository;
import com.bkav.lk.service.MedicationReminderService;
import com.bkav.lk.service.mapper.MedicationReminderMapper;
import com.bkav.lk.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicationReminderServiceImpl implements MedicationReminderService {

    private static final Logger log = LoggerFactory.getLogger(MedicationReminderServiceImpl.class);

    private static final String ENTITY_NAME = "medication_reminder";

    private final MedicationReminderRepository repository;

    private final MedicationReminderMapper mapper;

    public MedicationReminderServiceImpl(MedicationReminderRepository repository, MedicationReminderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MedicationReminderDTO save(MedicationReminderDTO dto) {
        MedicationReminder entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public MedicationReminderDTO findById(Long id) {
        Optional<MedicationReminder> reminder = repository.findById(id);
        return reminder.map(mapper::toDto).orElse(null);
    }

    @Override
    public MedicationReminderDTO findByUserIdAndBookingCodeAndTime(Long userId, String bookingCode, Integer time) {
        MedicationReminder entity = repository.findByUserIdAndBookingCodeAndTime(userId, bookingCode, time);
        if (entity != null) {
            return mapper.toDto(entity);
        }
        return null;
    }

    @Override
    public List<MedicationReminderDTO> findAllByTimeEqual(Integer time) {
        return mapper.toDto(repository.findAllByTimeAndStatus(time, Constants.ENTITY_STATUS.ACTIVE));
    }

    @Override
    public List<MedicationReminderDTO> findAllByBookingCode(Long userId, String bookingCode) {
        return mapper.toDto(repository.findAllByUserIdAndBookingCode(userId, bookingCode));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
