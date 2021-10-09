package com.bkav.lk.service;

import com.bkav.lk.dto.PatientRecordDTO;
import com.bkav.lk.web.rest.vm.HisPatientRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

public interface PatientRecordService {

    Optional<PatientRecordDTO> findOne(Long id);

    List<PatientRecordDTO> findByUserId(Long userId);

    List<PatientRecordDTO> getPhone(String code);

    List<PatientRecordDTO> findByAreaCode(String code);

    Page<PatientRecordDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    PatientRecordDTO save(PatientRecordDTO patientRecordDTO);

    boolean existsByHealthInsuranceCode(String healthInsuranceCode);

    PatientRecordDTO delete(Long patientRecordId);

    PatientRecordDTO findByCode(String patientCode);

    boolean existsPersonalPatientRecord();

    boolean existsPersonalPatientRecord(Long id);

    boolean existsRelativePatientRecord(Long id);

    HisPatientRecord convertToHisPatientRecord(String bookingCode);
}
