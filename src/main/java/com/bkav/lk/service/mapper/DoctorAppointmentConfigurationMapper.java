package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.DoctorAppointmentConfiguration;
import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorAppointmentConfigurationMapper extends EntityMapper<DoctorAppointmentConfigurationDTO, DoctorAppointmentConfiguration> {

    DoctorAppointmentConfiguration toEntity(DoctorAppointmentConfigurationDTO dto);

    DoctorAppointmentConfigurationDTO toDto(DoctorAppointmentConfiguration entity);

    default DoctorAppointmentConfiguration fromId(Long id){
        if(id == null){
            return null;
        }
        DoctorAppointmentConfiguration entity = new DoctorAppointmentConfiguration();
        entity.setId(id);
        return entity;
    }

}
