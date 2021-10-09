package com.bkav.lk.repository;

import com.bkav.lk.domain.DoctorSchedule;
import com.bkav.lk.repository.custom.DoctorScheduleRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long>, DoctorScheduleRepositoryCustom {

    Optional<DoctorSchedule> findOneByIdAndStatus(Long id, Integer status);

    List<DoctorSchedule> findAllByDoctor_IdAndStatusAndWorkingDateAfter(Long id, Integer status, Instant workingDate);

    List<DoctorSchedule> findAllByDoctor_IdAndStatusAndWorkingDateAfterAndWorkingDateBefore(Long id, Integer status, Instant startTime, Instant endTime);

    @Query("SElECT D FROM DoctorSchedule D WHERE D.status = 1")
    List<DoctorSchedule> findAll();

    Optional<DoctorSchedule> findByDoctorIdAndWorkingDateAndStatus(Long doctorId, Instant workingDate, Integer status);

    List<DoctorSchedule> findAllByDoctor_IdInAndWorkingDateEqualsAndStatus(List<Long> doctorIds, Instant workingDate, Integer status);

    List<DoctorSchedule> findAllByDoctor_IdInAndWorkingDateAndWorkingTimeInAndStatus(List<Long> doctorIds, Instant workingDate, List<Integer> workingTime, Integer status);

    List<DoctorSchedule> findAllByWorkingDateEqualsAndStatus(Instant workingDate, Integer status);

    @Query("SElECT D FROM DoctorSchedule D WHERE D.status = 1 AND D.id IN (:ids)")
    List<DoctorSchedule> findByIds(List<Long> ids);

    List<DoctorSchedule> findByDoctorIdAndStatusNot(Long doctorId, Integer status);

    List<DoctorSchedule> findByWorkingDateAndDoctorIdAndStatus(Instant workingDate, Long doctorId, Integer status);

    @Query(value = "SELECT DS FROM DoctorSchedule DS INNER JOIN DS.clinic C " +
            "WHERE DS.status <> :status AND C.id = :clinicId AND C.status <> :status")
    List<DoctorSchedule> findByClinicAndStatusNot(@Param("clinicId") Long clinicId, @Param("status") Integer status);

    @Query(value = "SELECT DS FROM DoctorSchedule DS INNER JOIN DS.clinic C " +
            "WHERE DS.status = :status AND C.id = :clinicId AND C.status = :status")
    List<DoctorSchedule> findByClinicAndStatus(@Param("clinicId") Long clinicId, @Param("status") Integer status);

    boolean existsByDoctorIdIn(List<Long> doctorIds);

    List<DoctorSchedule> findByDoctorIdInAndWorkingDateGreaterThanEqual(List<Long> doctorIds, Instant toInstant);
}
