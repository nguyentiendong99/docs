package com.bkav.lk.repository;

import com.bkav.lk.domain.MedicalSpeciality;
import com.bkav.lk.repository.custom.MedicalSpecialityRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalSpecialityRepository extends JpaRepository<MedicalSpeciality, Long>, MedicalSpecialityRepositoryCustom {

    List<MedicalSpeciality> findByHealthFacilitiesIdAndStatus(Long healthFacilityId, Integer status);

    Optional<MedicalSpeciality> findByCodeAndStatusGreaterThan(String s, Integer status);

    boolean existsByCodeAndStatusGreaterThan(String code, Integer status);

    @Query("SELECT M FROM MedicalSpeciality M WHERE M.healthFacilities.id = :healthFacilityId AND M.status IN (:status)")
    List<MedicalSpeciality> findByHealthFacilitiesIdAndStatus(Long healthFacilityId, Integer[] status);

    Optional<MedicalSpeciality> findByCodeAndStatus(String s, Integer status);
}
