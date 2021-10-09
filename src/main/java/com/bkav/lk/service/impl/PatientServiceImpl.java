package com.bkav.lk.service.impl;

import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.domain.Patient;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.PatientDTO;
import com.bkav.lk.repository.HealthFacilitiesRepository;
import com.bkav.lk.repository.PatientRepository;
import com.bkav.lk.service.PatientService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.PatientMapper;
import com.bkav.lk.web.rest.vm.HisPatientContentVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private final HealthFacilitiesRepository healthFacilitiesRepository;
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final UserService userService;

    @Autowired
    public PatientServiceImpl(PatientRepository patientRepository, HealthFacilitiesRepository healthFacilitiesRepository
            , PatientMapper patientMapper, UserService userService) {
        this.patientRepository = patientRepository;
        this.healthFacilitiesRepository = healthFacilitiesRepository;
        this.patientMapper = patientMapper;
        this.userService = userService;
    }

    @Override
    public PatientDTO save(PatientDTO patientDTO) {
        Patient patient = patientMapper.toEntity(patientDTO);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    @Override
    public PatientDTO savePatientInformation(HisPatientContentVM patientContent) {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setPatientCode(patientContent.getConnectionCode());
        patientDTO.setPatientName(patientContent.getPatientRecordName());
        patientDTO.setPhone(patientContent.getPatientRecordPhone());
        patientDTO.setHealthFacilityCode(patientContent.getHealthFacilityCode());
        return this.save(patientDTO);
    }

    @Override
    public PatientDTO findByPatientCodeAndPatientNameAndHealthFacility(String patientCode, String patientName, String code) {
        List<Patient> patients = this.patientRepository.findByPatientCodeAndPatientNameAndHealthFacilityCode(patientCode, patientName, code);
        return !CollectionUtils.isEmpty(patients) ? patientMapper.toDto(patients.get(0)) : null;
    }

    @Override
    public List<PatientDTO> findByCurrentUserAndHealthFacility(Long healthFacilityId) {
        List<Patient> results = null;
        User user = userService.getUserWithAuthorities().orElse(null);
        Optional<HealthFacilities> healthFacilities = healthFacilitiesRepository.findById(healthFacilityId);
        if (healthFacilities.isPresent()) {
            results = patientRepository.findByCreatedByAndHealthFacilityCode(user.getLogin(), healthFacilities.get().getCode());
        }
        return patientMapper.toDto(results);
    }

    @Override
    public PatientDTO findByOldAppointmentCode(String appointmentCode) {
        Optional<Patient> result = Optional.empty();
        Optional<User> user = userService.getUserWithAuthorities();
        if (user.isPresent()) {
            result = patientRepository.findByOldAppointmentCodeAndLogin(
                    appointmentCode, user.get().getLogin());
        }
        return result.map(patientMapper::toDto).orElse(null);
    }

}
