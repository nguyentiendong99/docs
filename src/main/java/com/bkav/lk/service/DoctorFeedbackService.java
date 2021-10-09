package com.bkav.lk.service;

import com.bkav.lk.domain.DoctorFeedback;
import com.bkav.lk.dto.DoctorFeedbackCustomConfigDTO;
import com.bkav.lk.dto.DoctorFeedbackDTO;
import com.bkav.lk.dto.PositionHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

public interface DoctorFeedbackService {

    Page<DoctorFeedbackDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    DoctorFeedbackDTO save(DoctorFeedbackDTO doctorFeedbackDTO);

    DoctorFeedback createFeedback(DoctorFeedbackDTO doctorFeedbackDTO);

    DoctorFeedbackDTO findById(Long id);

    Optional<DoctorFeedbackDTO> findOne(Long id);

    List<PositionHistoryDTO> getPositionHistory(MultiValueMap<String, String> queryParam);

    List<DoctorFeedbackCustomConfigDTO> findAllCustomConfigByFeedbackId(Long id, Long healthFacilityId);
}
