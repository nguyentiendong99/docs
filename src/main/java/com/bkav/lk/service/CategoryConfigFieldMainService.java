package com.bkav.lk.service;

import com.bkav.lk.dto.CategoryConfigFieldMainDTO;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface CategoryConfigFieldMainService {

    List<CategoryConfigFieldMainDTO> search(MultiValueMap<String, String> queryParams, Long healthFacilityId);

    List<CategoryConfigFieldMainDTO> searchReport(MultiValueMap<String, String> queryParams, Long healthFacilityId);

    List<CategoryConfigFieldMainDTO> searchPatient(MultiValueMap<String, String> queryParams, Long healthFacilityId);

    List<CategoryConfigFieldMainDTO> update(List<CategoryConfigFieldMainDTO> configFieldMainDTOs);
}
