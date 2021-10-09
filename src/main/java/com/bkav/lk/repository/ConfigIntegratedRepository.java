package com.bkav.lk.repository;

import com.bkav.lk.domain.ConfigIntegrated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigIntegratedRepository extends JpaRepository<ConfigIntegrated, Long> {

    List<ConfigIntegrated> findByHealthFacilityId(Long healthFacilityId);

    Optional<ConfigIntegrated> findByConnectCodeAndHealthFacilityIdAndStatus(String connectCode, Long healthFacilityId, Integer status);

    List<ConfigIntegrated> findAllByConnectCodeAndStatus(String connectCode, Integer status);
}
