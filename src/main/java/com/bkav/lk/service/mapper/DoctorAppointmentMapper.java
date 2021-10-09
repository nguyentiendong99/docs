package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.dto.DoctorAppointmentDTO;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@DecoratedWith(DoctorAppointmentDecorator.class)
@Mapper(componentModel = "spring", uses = {DoctorMapper.class, ClinicMapper.class, MedicalServiceMapper.class, DoctorMapper.class})
public interface DoctorAppointmentMapper extends EntityMapper<DoctorAppointmentDTO, DoctorAppointment> {

    @Mappings({
            @Mapping(source = "doctor.name", target = "doctorName"),
            @Mapping(source = "patientRecord.name", target = "patientName"),
            @Mapping(source = "patientRecord.patientRecordCode", target = "patientCode"),
            @Mapping(source = "patientRecord.dob", target = "patientDob"),
            @Mapping(source = "patientRecord.address", target = "patientAddress"),
            @Mapping(source = "patientRecord.id", target = "patientRecordId"),
            @Mapping(source = "clinic.id", target = "clinicId"),
            @Mapping(source = "clinic.name", target = "clinicName"),
            @Mapping(source = "medicalService.id", target = "medicalServiceId"),
            @Mapping(source = "medicalService.name", target = "medicalServiceName"),
            @Mapping(source = "medicalService.price", target = "medicalServicePrice"),
            @Mapping(source = "patientRecord.userId", target = "userId"),
            @Mapping(source = "doctor.academic.code", target = "academicCode"),
            @Mapping(source = "medicalSpeciality.id", target = "medicalSpecialityId")
    })
    DoctorAppointmentDTO toDto(DoctorAppointment doctorAppointment);

    @Mappings({
            @Mapping(source = "patientRecordId", target = "patientRecord.id"),
            @Mapping(source = "medicalServiceId", target = "medicalService.id"),
            @Mapping(source = "medicalServiceName", target = "medicalService.name"),
            @Mapping(source = "medicalServicePrice", target = "medicalService.price"),
    })
    DoctorAppointment toEntity(DoctorAppointmentDTO doctorAppointmentDTO);

    default DoctorAppointment fromId(Long id) {
        if (id == null) {
            return null;
        }
        DoctorAppointment doctorAppointment = new DoctorAppointment();
        doctorAppointment.setId(id);
        return doctorAppointment;
    }
}
