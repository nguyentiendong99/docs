package com.bkav.lk.repository;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.repository.custom.DoctorAppointmentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAppointmentRepository extends JpaRepository<DoctorAppointment, Long>, DoctorAppointmentRepositoryCustom {

    Integer countByStatusAndHealthFacilityId(Integer status, Long healthFacilityId);

    @Query("SELECT count(da) FROM DoctorAppointment da WHERE da.doctor.id = :doctorId AND da.status = :status AND da.healthFacilityId = :healthFacilityId")
    Integer countByDoctorIdAndStatusAndHealthFacilityId(Long doctorId, Integer status, Long healthFacilityId);

    List<DoctorAppointment> findAllByHealthFacilityIdAndStatusInAndStartTimeIsGreaterThanEqual(Long healthFacilityId, Integer[] status, Instant now);

    @Query("SELECT da FROM DoctorAppointment da WHERE da.status IN (:listStatus) AND da.endTime < :now")
    List<DoctorAppointment> findDoctorAppointmentStatusNotDone(Instant now, Integer[] listStatus);

    @Query("SELECT count(da) FROM DoctorAppointment da WHERE da.healthFacilityId = :healthFacilityId AND da.doctor.id = :doctorId AND da.startTime >= :startTime AND da.endTime <= :endTime AND da.status NOT IN (-1, 0)")
    Integer countDoctorAppointmentInRangeTime(
            @Param("healthFacilityId") Long healthFacilityId,
            @Param("doctorId") Long doctorId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("SELECT da FROM DoctorAppointment da WHERE da.status = :status AND da.healthFacilityId = :healthFacilityId AND da.doctor.id = :doctorId AND da.startTime = :startTime AND da.endTime = :endTime AND da.createdDate < :range")
    List<DoctorAppointment> findTempDoctorAppointmentInvalidWithDoctor(
            @Param("status") Integer status,
            @Param("healthFacilityId") Long healthFacilityId,
            @Param("doctorId") Long doctorId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("range") Instant range);

    @Query("SELECT da FROM DoctorAppointment da WHERE da.status = :status AND da.healthFacilityId = :healthFacilityId AND da.doctor.id IS NULL AND da.startTime = :startTime AND da.endTime = :endTime AND da.createdDate < :range")
    List<DoctorAppointment> findTempDoctorAppointmentInvalidNotWithDoctor(
            @Param("status") Integer status,
            @Param("healthFacilityId") Long healthFacilityId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("range") Instant range);

    @Query("SELECT da FROM DoctorAppointment da WHERE da.status = :status AND da.healthFacilityId = :healthFacilityId AND da.startTime = :startTime AND da.endTime = :endTime AND da.createdDate < :range")
    List<DoctorAppointment> findTempDoctorAppointmentInvalidBothDoctorAndNotDoctor(
            @Param("status") Integer status,
            @Param("healthFacilityId") Long healthFacilityId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("range") Instant range);

    @Query("SELECT da FROM DoctorAppointment da WHERE da.status = :status AND da.doctor.id = :doctorId AND da.startTime = :startTime AND da.endTime = :endTime AND da.createdDate < :range")
    List<DoctorAppointment> findTempDoctorAppointmentInvalidWithDoctor(
            @Param("status") Integer status,
            @Param("doctorId") Long doctorId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("range") Instant range);

    List<DoctorAppointment> findByIdIn(List<Long> ids);

    boolean existsByDoctorIdAndStatusNotIn(Long doctorId, Integer[] status);

    DoctorAppointment findByAppointmentCode(String appointmentCode);

    boolean existsByPatientRecordIdAndStatusNotIn(Long patientRecordId, Integer[] status);

    @Query("SELECT  d FROM DoctorAppointment d WHERE d.bookingCode=:bookingCode and d.status = 2")
    Optional<DoctorAppointment> findByBookingCode(String bookingCode);

    Optional<DoctorAppointment> findByBookingCodeAndStatus(String bookingCode, Integer status);

    DoctorAppointment findTopByBookingCode(String bookingCode);

    @Query("SELECT d FROM DoctorAppointment d WHERE d.startTime > :now AND d.endTime <= :nextDay AND d.status = 2")
    List<DoctorAppointment> findAllReminderAppointment(Instant now, Instant nextDay);

    @Query(value = "SELECT DA FROM DoctorAppointment DA INNER JOIN DA.clinic C " +
            "WHERE C.id = :clinicId AND C.status <> :status")
    List<DoctorAppointment> findByClinicAndStatusNot(Long clinicId, Integer status);

    @Query(value = "SELECT DA FROM DoctorAppointment DA INNER JOIN DA.clinic C " +
            "WHERE C.id = :clinicId AND C.status = :status")
    List<DoctorAppointment> findByClinicAndStatus(Long clinicId, Integer status);

    List<DoctorAppointment> findByDoctorId(Long doctorId);

    List<DoctorAppointment> findByHealthFacilityIdAndStatusIn(Long healthFacilityId, Integer[] status);

    List<DoctorAppointment> findByStatus(Integer status);

    boolean existsByAppointmentCode(String appointmentCode);
}
