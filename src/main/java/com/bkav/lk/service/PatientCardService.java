package com.bkav.lk.service;

import com.bkav.lk.dto.PatientCardDTO;

import java.util.List;

public interface PatientCardService {
    PatientCardDTO save(PatientCardDTO patientCardDTO);

    List<PatientCardDTO> findAll(Long userId);

    PatientCardDTO update(PatientCardDTO patientCardDTO);

    void delete(Long id);

    PatientCardDTO findByCardNumber(String cardNumber);

    PatientCardDTO findByUserIdAndId(Long userId, Long id);
}
