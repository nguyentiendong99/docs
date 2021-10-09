package com.bkav.lk.service;

import com.bkav.lk.dto.PatientDTO;
import com.bkav.lk.web.rest.vm.HisPatientContentVM;

import java.util.List;

public interface PatientService {

    PatientDTO save(PatientDTO patientDTO);

    PatientDTO savePatientInformation(HisPatientContentVM patientContent);

    PatientDTO findByPatientCodeAndPatientNameAndHealthFacility(String patientCode, String patientName, String code);

    List<PatientDTO> findByCurrentUserAndHealthFacility(Long healthFacilityId);

    PatientDTO findByOldAppointmentCode(String appointmentCode);
}
