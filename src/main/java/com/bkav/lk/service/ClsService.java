package com.bkav.lk.service;

import com.bkav.lk.dto.ClsCustomConfigDTO;
import com.bkav.lk.dto.ClsDTO;
import com.bkav.lk.web.errors.ErrorExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface ClsService {
    ClsDTO save(ClsDTO clsDTO);

    Page<ClsDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    void delete(Long id);

    Optional<ClsDTO> findOne(Long id);

    List<ClsDTO> excelToObject(InputStream inputStream, List<ErrorExcel> details);

    ByteArrayInputStream exportToExcel(List<ClsDTO> list, InputStream file);

    ClsDTO findByIdAndStatus(Long id);

    /**
     * Find all custom config by cls id list.
     *
     * @param clsId the doctor id
     * @return the list
     */
    List<ClsCustomConfigDTO> findAllCustomConfigByClsId(Long clsId);

    List<ClsDTO> findAllByIds(List<Long> ids);
}
