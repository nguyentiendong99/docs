package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.Patient;

import java.util.Optional;

public interface PatientRepositoryCustom {

    Optional<Patient> findByOldAppointmentCodeAndLogin(String appointmentCode, String login);

}
