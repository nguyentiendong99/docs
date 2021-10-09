package com.bkav.lk.service;

import com.bkav.lk.dto.CategoryConfigIconDTO;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface CategoryConfigIconService {
    List<CategoryConfigIconDTO> search(MultiValueMap<String, String> queryParams, Long healthFacilityId);

    List<CategoryConfigIconDTO> searchReport(MultiValueMap<String, String> queryParams, Long healthFacilityId);

    List<CategoryConfigIconDTO> searchPatient(MultiValueMap<String, String> queryParams, Long healthFacilityId);

    List<CategoryConfigIconDTO> update(List<CategoryConfigIconDTO> configIconDTOs);
}
