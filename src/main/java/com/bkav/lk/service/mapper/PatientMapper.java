package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Patient;
import com.bkav.lk.dto.PatientDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper extends EntityMapper<PatientDTO, Patient> {

    Patient toEntity(PatientDTO patientDTO);

    PatientDTO toDto(Patient patient);

    default Patient fromId(Long id) {
        if (id == null) {
            return null;
        }
        Patient patient = new Patient();
        patient.setId(id);
        return patient;
    }
}
