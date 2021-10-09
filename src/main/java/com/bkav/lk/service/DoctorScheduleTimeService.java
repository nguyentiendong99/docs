package com.bkav.lk.service;

import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.dto.DoctorScheduleTimeDTO;
import com.bkav.lk.dto.DoctorScheduleTimeVM;

import java.time.Instant;
import java.util.List;

public interface DoctorScheduleTimeService {

    DoctorScheduleTimeDTO findOne(Long doctorId, Instant startTime, Instant endTime, Long healthFacilityId);

    DoctorScheduleTimeDTO findOne(Instant startTime, Instant endTime, Long healthFacilityId);

    DoctorScheduleTimeDTO findOne(Long doctorId, Instant startTime, Instant endTime);

    boolean appointmentTimeAvailable(Long healthFacilityId, Long doctorId, Instant startTime, Instant endTime, int numberOfItem, boolean isCreateNew, boolean isChangeSchedule);

    DoctorScheduleTimeDTO save(DoctorScheduleTimeDTO doctorScheduleTimeDTO);

    DoctorScheduleTimeDTO create(Long doctorId, Instant startTime, Instant endTime, int numberOfItem, Long healthFacilityId);

    void minusSubscriptions(Long doctorId, Instant startTime, Instant endTime, int numberOfItem, Long healthFacilityId);

    // isNotFoundWillCreateNew = true => nếu không tìm thấy record trong DoctorAppointmentTime => tạo mới
    void plusSubscriptions(Long doctorId, Instant startTime, Instant endTime, Long healthFacilityId, boolean isNotFoundWillCreateNew);

    List<DoctorScheduleTimeVM> findSchedulesOfHospitalAvailable(Long healthFacilityId, DoctorAppointmentConfigurationDTO config, String day, List<DoctorDTO> doctorDTOList, boolean isRandomTimeOption, boolean isMorning);

    List<DoctorScheduleTimeVM> findSchedulesOfDoctorAvailable(Long healthFacilityId, DoctorAppointmentConfigurationDTO dto, String day, Long doctorId, boolean isRandomTimeOption, boolean isMorning);

}
