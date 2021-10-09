package com.bkav.lk.repository;

import com.bkav.lk.domain.MedicalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalResultRepository extends JpaRepository<MedicalResult, Long> {

    List<MedicalResult> findByPatientIdAndCreatedBy(Long patientId, String login);

    List<MedicalResult> findByCreatedBy(String createdBy);
}
