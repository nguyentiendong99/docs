package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.CategoryConfigIcon;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface CategoryConfigIconRepositoryCustom {
    List<CategoryConfigIcon> search(MultiValueMap<String, String> queryParams, Long healthFacilityId);
}
