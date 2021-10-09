package com.bkav.lk.repository;

import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.repository.custom.HealthFacilitiesRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthFacilitiesRepository extends JpaRepository<HealthFacilities, Long> , HealthFacilitiesRepositoryCustom {
    @Query("SElECT H FROM HealthFacilities H WHERE H.status = 1 OR H.status =2 ORDER BY H.createdDate DESC")
    List<HealthFacilities> findByStatusActiveOrUnActive();

    @Query("SElECT H FROM HealthFacilities H WHERE H.status IN (:status)")
    List<HealthFacilities> findByStatus(List<Integer> status);

    Optional<HealthFacilities> findById(Long id);

    boolean existsByCodeAndStatusGreaterThan(String code, Integer status);

    List<HealthFacilities> findByParentAndStatus(Long parentId, Integer status);

    Optional<HealthFacilities> findByCode(String code);

    List<HealthFacilities> findAllByParentCodeAndStatus(String parentCode, Integer status);

    Optional<HealthFacilities> findByCodeAndStatusGreaterThan(String code, Integer status);

    List<HealthFacilities> findAllByParentAndStatusGreaterThan(Long parent, Integer status);

    /**
     * Exists by id and status boolean.
     *
     * @param id     the id
     * @param status the status
     * @return the boolean
     */
    boolean existsByIdAndStatus(Long id, Integer status);
}
