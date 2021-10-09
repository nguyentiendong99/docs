package com.bkav.lk.service;

import com.bkav.lk.domain.Clinic;
import com.bkav.lk.dto.ClinicCustomConfigDTO;
import com.bkav.lk.dto.ClinicDTO;
import com.bkav.lk.web.errors.ErrorExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface ClinicService {

    Optional<Clinic> findOne(Long id);

    List<ClinicDTO> findAll();

    ClinicDTO findByCode(String code);

    ClinicDTO findByIdAndStatus(Long id);

    Page<ClinicDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Clinic save(ClinicDTO clinicDTO);
    
    void delete(Long id);

    ByteArrayInputStream exportClinicToExcel(List<ClinicDTO> list, InputStream file);

    List<ClinicDTO> findByHealthFacilityId(Long healthFacilityId);

    String generateClinicCode(String code, String name, boolean checkName);

    List<ClinicDTO> excelToClinic(InputStream inputStream, List<ErrorExcel> details);

    List<ClinicCustomConfigDTO> findAllCustomConfigByClinicId(Long id);

    boolean isDeactivable (Long clinicId);

    List<ClinicDTO> findAllByIds(List<Long> ids);

}
