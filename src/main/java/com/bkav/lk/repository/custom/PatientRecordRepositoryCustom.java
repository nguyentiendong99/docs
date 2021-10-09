package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.PatientRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface PatientRecordRepositoryCustom {

    List<PatientRecord> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    List<PatientRecord> getPhoneByPatient(String code, Integer status);
}
