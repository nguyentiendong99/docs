package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Doctor;
import com.bkav.lk.dto.DoctorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface DoctorMapper extends EntityMapper<DoctorDTO, Doctor> {

    @Mappings({
            @Mapping(target = "academic.id", source = "doctorDTO.academicId"),
            @Mapping(target = "academic.name", source = "doctorDTO.academicName"),
            @Mapping(target = "academic.code", source = "doctorDTO.academicCode"),
            @Mapping(target = "medicalSpeciality.id", source = "doctorDTO.medicalSpecialityId"),
            @Mapping(target = "medicalSpeciality.name", source = "doctorDTO.medicalSpecialityName"),
            @Mapping(target = "clinic.id", source = "doctorDTO.clinicId")
    })
    Doctor toEntity(DoctorDTO doctorDTO);

    @Mappings({
            @Mapping(target = "academicId", source = "doctor.academic.id"),
            @Mapping(target = "academicName", source = "doctor.academic.name"),
            @Mapping(target = "academicCode", source = "doctor.academic.code"),
            @Mapping(target = "medicalSpecialityId", source = "doctor.medicalSpeciality.id"),
            @Mapping(target = "medicalSpecialityName", source = "doctor.medicalSpeciality.name"),
            @Mapping(target = "clinicId", source = "doctor.clinic.id"),
            @Mapping(target = "clinicName", source = "doctor.clinic.name")
    })
    DoctorDTO toDto(Doctor doctor);

    default Doctor fromId(Long id) {
        if (id == null)
            return null;
        Doctor doctor = new Doctor();
        doctor.setId(id);
        return doctor;
    }
}
