package com.bkav.lk.service;

import com.bkav.lk.dto.AcademicDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

public interface AcademicService {

    Optional<AcademicDTO> findOne(Long id);

    Boolean checkExistCode(String code);

    Page<AcademicDTO> search(MultiValueMap<String,String> queryParams, Pageable pageable);

    AcademicDTO findByAcademicName(String name);

    AcademicDTO save(AcademicDTO academicDTO);

    void delete(Long id);

    boolean existCode(String code);

    List<AcademicDTO> findAll();
}
