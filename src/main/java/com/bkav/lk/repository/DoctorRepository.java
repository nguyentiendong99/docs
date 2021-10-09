package com.bkav.lk.repository;

import com.bkav.lk.domain.Doctor;
import com.bkav.lk.repository.custom.DoctorRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>, DoctorRepositoryCustom {

    List<Doctor> findAllByPositionIdAndStatus(Long positionId, Integer status);

    List<Doctor> findAllByHealthFacilityIdAndStatus(Long healthFacilityId, Integer status);

    Optional<Doctor> findByCode(String code);

    Optional<Doctor> findByIdAndStatus(Long doctorId, Integer status);

    Boolean existsByAcademicId(Long id);

    Optional<Doctor> findByCodeAndStatusIsGreaterThanEqual(String code, Integer active);

    @Query("SElECT D FROM Doctor D WHERE D.status = :status And D.code IN (:codes) And D.healthFacilityId = :healthFacilityId")
    List<Doctor> findByCodesAndHealthFacilityId(List<String> codes, int status, Long healthFacilityId);

    Optional<Doctor> findByCodeAndStatusGreaterThan(String newCode, Integer status);

    @Query("SElECT D FROM Doctor D WHERE D.healthFacilityId = :healthFacilityId And D.status IN (:status)")
    List<Doctor> findAllByHealthFacilityIdAndStatus(Long healthFacilityId, Integer[] status);

    List<Doctor> findByMedicalSpecialityIdAndMedicalSpecialityStatusNot(Long medicalSpecialityId, Integer status);

    boolean existsByPositionIdAndStatus(Long positionId, Integer status);

    List<Doctor> findByClinicIdAndStatusIn(Long clinicId, Integer[] status);
}
