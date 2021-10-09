package com.bkav.lk.service;

import com.bkav.lk.domain.User;
import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import com.bkav.lk.dto.DoctorScheduleDTO;
import com.bkav.lk.service.util.ResultExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DoctorScheduleService {

    Page<DoctorScheduleDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable, User user);

    Optional<DoctorScheduleDTO> findOne(Long id);

    List<DoctorScheduleDTO> findSchedulesOfDoctor(Long id);

    DoctorScheduleDTO save(DoctorScheduleDTO doctorScheduleDTO);

    void delete(Long id);

    List<DoctorScheduleDTO> saveAll(Long healthFacilityId, List<DoctorScheduleDTO> doctorScheduleDTOs);

    boolean isDoctorSchedulesExist(List<DoctorScheduleDTO> doctorScheduleDTOs);

    boolean isDoctorScheduleExist(DoctorScheduleDTO doctorScheduleDTO);

    Map<String, List<DoctorScheduleDTO>> handleTree(List<DoctorScheduleDTO> list);

    List<DoctorScheduleDTO> findAll();

    List<DoctorScheduleDTO> findAllByWorkingDateAndStatus(List<Long> doctorIds, Instant workingDate, Integer status);

    List<DoctorScheduleDTO> findSchedulesOfDoctorValid(Long doctorId, Instant startTime, Instant endTime);

    List<Instant> findAllAvailableInHospital(Long healthFacilityId, Instant startDate, Instant endDate);

    ResultExcel bulkUploadSchedules(Long healthFacilityId, List<DoctorScheduleDTO> list);

    List<DoctorScheduleDTO> excelToSchedules(InputStream inputStream);

    ByteArrayInputStream exportDoctorScheduleToExcel(List<DoctorScheduleDTO> list, InputStream file);

    List<DoctorScheduleDTO> findByIds(List<Long> ids);

    Map<String, Object> timeOfDayValid(Long healthFacilityId);

    DayOfWeek getDayOfWeekByDateString(String date);

    Integer getWorkingTimeByDateByConfig(DayOfWeek dayOfWeek, List<Integer> dayOfWeekMorningValid, List<Integer> dayOfWeekAfternoonValid);

    List<DoctorScheduleDTO> findByClinicAndStatusNot(Long id, Integer status);

    InputStream downloadTemplateExcelDoctorSchedule();

    boolean existsPendingConfigWithHealthFacility(Long healthFacilityId);

    boolean existsByHealthFacilityId(Long healthFacilityId);

    void updateViolatedSchedule(DoctorAppointmentConfigurationDTO currentActiveAppointmentConfig, DoctorAppointmentConfigurationDTO pendingAppointmentConfig);

    DoctorScheduleDTO findByDoctorIdAndWorkingDateAndStatus(Long doctorId, Instant workingDate, Integer status);
}
