package com.bkav.lk.service;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.DoctorAppointmentConfigurationDTO;
import com.bkav.lk.dto.DoctorAppointmentDTO;
import com.bkav.lk.dto.DoctorAppointmentHistoryDTO;
import com.bkav.lk.dto.NotifyDoctorAppointmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DoctorAppointmentService {

    Page<DoctorAppointmentDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable, User user);

    Optional<DoctorAppointmentDTO> findOne(Long id);

    List<DoctorAppointmentDTO> findAllByIds(List<Long> ids);

    DoctorAppointmentDTO findByAppointmentCode(String appointmentCode);

    void approve(String ids);

    void deny(String ids, String rejectReason);

    DoctorAppointmentDTO confirm(Long id);

    DoctorAppointmentDTO cancel(Long id);

    Object update(List<DoctorAppointmentDTO> listDoctorAppointmentDTO);

    Integer countByStatus(Integer status, Long healthFacilityId);

    Integer countByStatus(Long doctorId, Integer status, Long healthFacilityId);

    DoctorAppointmentDTO save(DoctorAppointmentDTO doctorAppointmentDTO);

    DoctorAppointmentDTO saveNormal(DoctorAppointmentDTO doctorAppointmentDTO);

    List<NotifyDoctorAppointmentDTO> getDoctorAppointments(List<Long> ids);

    void schedulingDoctorAppointmentJob();

    List<String> getAppointmentCodesByPatientId(Long patientId, Long healthFacilityId, Integer status);

    List<DoctorAppointmentHistoryDTO> getHistory(Long id);

    boolean existByDoctorId(Long doctorId);

    boolean existByPatientRecordId(Long patientRecordId);

    List<DoctorAppointmentDTO> findAll();

    List<DoctorAppointmentDTO> findByIds(List<Long> ids);

    ByteArrayInputStream exportToExcel(List<DoctorAppointmentDTO> list, InputStream file);

    void updatePayStatus(Long id);

    List<DoctorAppointmentDTO> findByHealthFacilityAndPatient(MultiValueMap<String, String> queryParams);

    Optional<DoctorAppointmentDTO> findByBookingCode(String bookingCode);

    Optional<DoctorAppointmentDTO> findByBookingCodeAndStatus(String bookingCode, Integer status);

    DoctorAppointmentDTO findTopByBookingCode(String bookingCode);

    List<DoctorAppointmentDTO> schedulingReminderAppointmentJob();

    List<DoctorAppointmentDTO> findByClinicAndStatusNot(Long id, Integer status);

    Integer countHealthFacilityAndDoctorId(Long healthFacilityId, Long doctorId, Instant startTime, Instant endTime);

    List<DoctorAppointmentDTO> findByHealthFacility(Long healthFacilityId, Integer[] status);

    void schedulingDoctorAppointmentReminderNotificationJob(List<DoctorAppointmentConfigurationDTO> list);

    ByteArrayInputStream exportToExcelReExam(List<DoctorAppointmentDTO> list, InputStream file);

    boolean existsByAppointmentCode(String appointmentCode);

    List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidWithDoctor(Integer status, Long healthFacilityId, Long doctorId, Instant startTime, Instant endTime, Integer timeout);

    List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidWithDoctor(Integer status, Long doctorId, Instant startTime, Instant endTime, Integer timeout);

    List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidNotWithDoctor(Integer status, Long healthFacilityId, Instant startTime, Instant endTime, Integer timeout);

    List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidBothDoctorAndNotDoctor(Integer status, Long healthFacilityId, Instant startTime, Instant endTime, Integer timeout);

    void deleteTempDoctorAppointment(List<Long> ids);

    List<DoctorAppointmentDTO> findAllByHealthFacilityIdStatusInAndStartTimeIsGreaterThanEqual(Long healthFacilityId, Integer[] status, Instant now);
}
