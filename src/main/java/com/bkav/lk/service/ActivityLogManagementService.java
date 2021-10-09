package com.bkav.lk.service;

import com.bkav.lk.dto.ActivityLogForManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;


public interface ActivityLogManagementService {

    Page<ActivityLogForManagementDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);
}
