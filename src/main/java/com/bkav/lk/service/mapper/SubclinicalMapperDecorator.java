package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.domain.Subclinical;
import com.bkav.lk.dto.SubclinicalDTO;
import com.bkav.lk.repository.DoctorAppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class SubclinicalMapperDecorator implements SubclinicalMapper {

    @Autowired
    private SubclinicalMapper delegate;

    @Autowired
    private DoctorAppointmentRepository doctorAppointmentRepository;

    @Override
    public Subclinical toEntity(SubclinicalDTO subclinicalDTO) {
        return delegate.toEntity(subclinicalDTO);
    }

    @Override
    public SubclinicalDTO toDto(Subclinical subclinical) {
        SubclinicalDTO dto = delegate.toDto(subclinical);
        if (Objects.isNull(dto.getPatientRecordId())) {
            DoctorAppointment doctorAppointment =
                    doctorAppointmentRepository.findByAppointmentCode(dto.getDoctorAppointmentCode());
            PatientRecord patientRecord = null;
            if (Objects.nonNull(doctorAppointment) && Objects.nonNull(doctorAppointment.getPatientRecord())) {
                patientRecord = doctorAppointment.getPatientRecord();
                dto.setPatientRecordId(patientRecord.getId());
                dto.setPatientRecordCode(patientRecord.getPatientRecordCode());
                dto.setPatientRecordName(patientRecord.getName());
            }
        }
        return dto;
    }

    @Override
    public List<Subclinical> toEntity(List<SubclinicalDTO> dtoList) {
        return delegate.toEntity(dtoList);
    }

    @Override
    public List<SubclinicalDTO> toDto(List<Subclinical> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }
        List<SubclinicalDTO> dtos = entityList.stream()
                .map(this::toDto).collect(Collectors.toList());
        return dtos;
    }

}
