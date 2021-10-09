package com.bkav.lk.repository;

import com.bkav.lk.domain.DoctorScheduleTime;
import com.bkav.lk.repository.custom.DoctorScheduleTimeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleTimeRepository extends JpaRepository<DoctorScheduleTime, Long>, DoctorScheduleTimeRepositoryCustom {

    @Query(value = "SELECT SUM(d.peopleRegistered) FROM DoctorScheduleTime d WHERE d.doctorId in (:doctorIds) AND d.startTime >= :startTime AND d.endTime <= :endTime AND d.healthFacilityId = :healthFacilityId")
    Integer totalSUMPeopleRegistered(@Param("doctorIds") List<Long> doctorIds, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime, @Param("healthFacilityId") Long healthFacilityId);

    @Query(value = "SELECT SUM(d.peopleRegistered) FROM DoctorScheduleTime d WHERE d.startTime >= :startTime AND d.endTime <= :endTime AND d.healthFacilityId = :healthFacilityId")
    Integer totalSUMPeopleRegisteredByTime(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime, @Param("healthFacilityId") Long healthFacilityId);

    @Query(value = "SELECT d FROM DoctorScheduleTime d WHERE d.doctorId IS NULL AND d.startTime >= :startTime AND d.endTime <= :endTime AND d.healthFacilityId = :healthFacilityId")
    Optional<DoctorScheduleTime> findByTime(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime, @Param("healthFacilityId") Long healthFacilityId);

    Optional<DoctorScheduleTime> findByDoctorIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndHealthFacilityId(Long id, Instant startTime, Instant endTime, Long healthFacilityId);

    Optional<DoctorScheduleTime> findByDoctorIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(Long id, Instant startTime, Instant endTime);

}
