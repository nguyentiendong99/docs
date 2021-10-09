package com.bkav.lk.service;

import com.bkav.lk.domain.FeedbackContent;
import com.bkav.lk.dto.FeedbackContentDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackContentService {
    FeedbackContent createFeedbackContent (Long idFeedback, String content, String type);

    FeedbackContentDTO findByFeedbackIdAndType(Long feedbackId, String type);

    List<FeedbackContentDTO> findListByFeedbackId(Long id);
}
