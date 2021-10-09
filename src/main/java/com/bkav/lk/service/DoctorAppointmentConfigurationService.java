package com.bkav.lk.service;

import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import com.bkav.lk.dto.DoctorAppointmentDTO;

import java.util.List;

public interface DoctorAppointmentConfigurationService {

    List<DoctorAppointmentConfigurationDTO> getListConfig();

    List<DoctorAppointmentConfigurationDTO> findAllPendingConfig();

    DoctorAppointmentConfigurationDTO getDefaultConfig();

    DoctorAppointmentConfigurationDTO save(DoctorAppointmentConfigurationDTO configurationDTO);

    DoctorAppointmentConfigurationDTO findOne(Long healthFacilitiesId, Integer status);

    DoctorAppointmentConfigurationDTO findOneByHealthFacilitiesId(Long healthFacilitiesId);

    void delete(Long healthFacilitiesId);

    void deleteById(Long id);

    DoctorAppointmentConfigurationDTO updateBothConfig(
            DoctorAppointmentConfigurationDTO mandatoryAppointmentConfig,
            DoctorAppointmentConfigurationDTO optionalAppointmentConfig,
            Integer optionalStatus,
            Integer applyConfigAfterDay);

    void updateDoctorAppointmentByConfig(List<DoctorAppointmentDTO> appointmentDTOList, Long healthFacilityId, DoctorAppointmentConfigurationDTO currentConfig);
}
