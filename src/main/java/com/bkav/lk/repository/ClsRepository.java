package com.bkav.lk.repository;

import com.bkav.lk.domain.Cls;
import com.bkav.lk.repository.custom.ClsRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClsRepository extends JpaRepository<Cls, Long>, ClsRepositoryCustom {
    Cls findTopByClsCode(String code);

    Optional<Cls> findByClsCodeAndStatusIsGreaterThanEqual(String code, Integer active);

    Optional<Cls> findByIdAndStatusIsNot(Long id, Integer status);

}
