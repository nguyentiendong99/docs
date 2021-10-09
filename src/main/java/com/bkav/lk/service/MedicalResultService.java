package com.bkav.lk.service;

import com.bkav.lk.dto.MedicalResultDTO;

import java.util.List;

public interface MedicalResultService {

    List<MedicalResultDTO> findByPatientId(Long patientId);

    List<MedicalResultDTO> saveAll(List<MedicalResultDTO> medicalResultDTOs);

    List<String> findByCurrentUser();
}
