package com.bkav.lk.repository;

import com.bkav.lk.domain.Clinic;
import com.bkav.lk.domain.MedicalSpeciality;
import com.bkav.lk.repository.custom.ClinicRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long>, ClinicRepositoryCustom{

    Clinic findByCode(String code);

    Optional<Clinic> findByCodeAndStatusIsNot(String code, Integer status);

    Optional<Clinic> findByIdAndStatusIsNot(Long id, Integer status);

    @Modifying
    @Query("update Clinic c set c.status =:status where c.id =:id")
    @Transactional
    void deleteTechnically(Long id, Integer status);

    List<Clinic> findByHealthFacilitiesIdAndStatus(Long healthFacilityId, Integer active);

    @Query(value = "SELECT C.* FROM clinic C INNER JOIN doctor D ON D.clinic_id = C.id WHERE D.id = :doctorId AND C.status = :status",
            nativeQuery = true)
    Optional<Clinic> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") Integer status);

    @Query(value = "SELECT C.* FROM clinic C INNER JOIN doctor D ON D.clinic_id = C.id WHERE D.id = :doctorId",
            nativeQuery = true)
    Optional<Clinic> findByDoctorId(@Param("doctorId") Long doctorId);
}
