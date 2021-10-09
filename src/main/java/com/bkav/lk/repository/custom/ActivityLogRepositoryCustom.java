package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface ActivityLogRepositoryCustom  {

    List<ActivityLog> search(MultiValueMap<String, String> queryParams);

    List<ActivityLog> searchForManagement(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

}
