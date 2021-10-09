package com.bkav.lk.service;

import com.bkav.lk.dto.SubclinicalDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface SubclinicalService {

    Page<SubclinicalDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    List<SubclinicalDTO> update(List<SubclinicalDTO> subclinicalDTO);

    ByteArrayInputStream exportToExcel(List<SubclinicalDTO> list, InputStream file);

    SubclinicalDTO save(SubclinicalDTO subclinicalDTO);

    boolean existsByDoctorAppointmentCode(String doctorAppointmentCode);

    List<SubclinicalDTO> findAllByIds(List<Long> ids);
}
