package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.CategoryConfigFieldMain;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface CategoryConfigFieldMainRepositoryCustom {
    List<CategoryConfigFieldMain> search(MultiValueMap<String, String> queryParams, Long healthFacilityId);
}
