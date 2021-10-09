package com.bkav.lk.repository;

import com.bkav.lk.domain.DoctorAppointmentConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAppointmentConfigurationRepository extends JpaRepository<DoctorAppointmentConfiguration, Long> {

    Optional<DoctorAppointmentConfiguration> findByHealthFacilitiesIdAndStatus(Long healthFacilitiesId, Integer status);

    Optional<DoctorAppointmentConfiguration> findByHealthFacilitiesId(Long healthFacilitiesId);

    boolean existsByHealthFacilitiesIdAndStatus(Long healthFacilityId, Integer status);

    List<DoctorAppointmentConfiguration> findByStatus(Integer status);
}
