package com.bkav.lk.service;

import com.bkav.lk.dto.FeedbackCustomConfigDTO;
import com.bkav.lk.dto.FeedbackDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import java.util.List;


public interface FeedbackService {

    Page<FeedbackDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    FeedbackDTO save(FeedbackDTO feedbackDTO);

    FeedbackDTO createFeedback (FeedbackDTO feedbackDTO);

    FeedbackDTO findById(Long id);

    List<FeedbackDTO> findByUserId(Long userId);

    void autoChangeStatusEndTime();

    List<FeedbackCustomConfigDTO> findAllCustomConfigByFeedbackId(Long id, Long healthFacilityId);

}
