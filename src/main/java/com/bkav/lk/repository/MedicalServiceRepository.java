package com.bkav.lk.repository;

import com.bkav.lk.domain.MedicalService;
import com.bkav.lk.repository.custom.MedicalServiceRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long>, MedicalServiceRepositoryCustom {

    Optional<MedicalService> findOneByIdAndStatus(Long id, Integer status);

    List<MedicalService> findAllByStatus(Integer status);

    boolean existsByCode(String code);

    MedicalService findTopByCode(String code);

    boolean existsByCodeAndStatusGreaterThan(String code, Integer status);

}
