package com.bkav.lk.service;

import com.bkav.lk.dto.MedicalServiceCustomConfigDTO;
import com.bkav.lk.dto.MedicalServiceDTO;
import com.bkav.lk.web.errors.ErrorExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface MedicalServiceService {

    Page<MedicalServiceDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Optional<MedicalServiceDTO> findOne(Long id);

    MedicalServiceDTO save(MedicalServiceDTO medicalServiceDTO);

    List<MedicalServiceDTO> findAll();

    void delete(Long id);

    boolean existCode(String code);

    ByteArrayInputStream exportToExcel(List<MedicalServiceDTO> list, InputStream file);

    List<MedicalServiceDTO> excelToOBject(InputStream inputStream, List<ErrorExcel> details);

    List<MedicalServiceCustomConfigDTO> findAllCustomConfigByMedicalServiceId(Long id);
    List<MedicalServiceDTO> findAllByIds(List<Long> ids);
}
