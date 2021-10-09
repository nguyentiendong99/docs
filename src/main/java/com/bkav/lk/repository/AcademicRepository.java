package com.bkav.lk.repository;

import com.bkav.lk.domain.Academic;
import com.bkav.lk.repository.custom.AcademicRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicRepository extends JpaRepository<Academic, Long>, AcademicRepositoryCustom {

    boolean existsByCode(String code);

    Optional<Academic> findAcademicByNameAndStatus(String name, Integer status);

    boolean existsByCodeAndStatus(String code, Integer status);

    Optional<Academic> findByCodeAndStatus(String tempAcademicCode, Integer status);

    List<Academic> findByStatus(Integer active);

    Academic findTopByCode (String code);
}
