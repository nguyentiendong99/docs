package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.HealthFacilities;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface HealthFacilitiesRepositoryCustom {
    List<HealthFacilities> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    List<HealthFacilities> findAllChildrenByParent(Long parent);
}
