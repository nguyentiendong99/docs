package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.dto.NotifyDoctorAppointmentDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface DoctorAppointmentRepositoryCustom {

    List<DoctorAppointment> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    void approve(String id);

    void deny(String id, String rejectReason);

    List<NotifyDoctorAppointmentDTO> getDoctorAppointments(List<Long> ids);

    void cancel(List<Long> ids);

    List<String> getAppointmentCodesByPatientId(Long patientId, Long healthFacilityId, Integer status);

    List<DoctorAppointment> findByHealthFacilityAndPatient(MultiValueMap<String,String> queryParams);

    void deleteTempDoctorAppointment(List<Long> ids);
}
