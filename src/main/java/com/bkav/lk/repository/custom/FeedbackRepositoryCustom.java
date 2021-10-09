package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.Feedback;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface FeedbackRepositoryCustom {

    List<Feedback> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);
}
