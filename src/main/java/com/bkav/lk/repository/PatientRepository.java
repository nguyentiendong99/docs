package com.bkav.lk.repository;

import com.bkav.lk.domain.Patient;
import com.bkav.lk.repository.custom.PatientRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, PatientRepositoryCustom {

    List<Patient> findByPatientCodeAndPatientNameAndHealthFacilityCode(String patientCode, String patientName, String healthFacilityCode);

    List<Patient> findByCreatedByAndHealthFacilityCode(String login, String code);
}
