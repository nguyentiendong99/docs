package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.MedicalService;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface MedicalServiceRepositoryCustom {

    List<MedicalService> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

}
