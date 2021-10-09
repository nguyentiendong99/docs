package com.bkav.lk.repository;

import com.bkav.lk.domain.VnpayInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VnpayInformationRepository extends JpaRepository<VnpayInformation, Long> {

    Optional<VnpayInformation> findByHealthFacilityId(Long healthFacilityId);
}
