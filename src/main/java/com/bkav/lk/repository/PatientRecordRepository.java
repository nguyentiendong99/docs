package com.bkav.lk.repository;

import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.repository.custom.PatientRecordRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRecordRepository extends JpaRepository<PatientRecord, Long>, PatientRecordRepositoryCustom {

    List<PatientRecord> findByUserIdAndStatus(Long userId, Integer status);

    boolean existsByHealthInsuranceCodeAndStatus(String healthInsuranceCode, Integer status);

    List<PatientRecord> findByPatientRecordCode(String patientCode);

    List<PatientRecord> findByDistrict_AreaCode(String districtCode);

    List<PatientRecord> findByCity_AreaCode(String cityCode);

    boolean existsByUserIdAndRelationshipAndStatusNot(Long userId, String relationship, Integer status);

    boolean existsByUserIdAndIdAndRelationshipAndStatusNot(Long userId, Long id, String relationship, Integer status);

    boolean existsByUserIdAndRelationshipNotAndStatusNot(Long userId, String relationship, Integer status);
}
