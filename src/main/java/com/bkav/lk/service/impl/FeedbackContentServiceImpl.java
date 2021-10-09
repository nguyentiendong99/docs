package com.bkav.lk.service.impl;

import com.bkav.lk.domain.FeedbackContent;
import com.bkav.lk.dto.FeedbackContentDTO;
import com.bkav.lk.repository.FeedbackContentRepository;
import com.bkav.lk.service.FeedbackContentService;
import com.bkav.lk.service.mapper.FeedbackContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackContentServiceImpl implements FeedbackContentService {
    private final FeedbackContentRepository feedbackContentRepository;

    private final FeedbackContentMapper feedbackContentMapper;

    @Autowired
    public FeedbackContentServiceImpl(FeedbackContentRepository feedbackContentRepository, FeedbackContentMapper feedbackContentMapper) {
        this.feedbackContentRepository = feedbackContentRepository;
        this.feedbackContentMapper = feedbackContentMapper;
    }

    @Override
    public FeedbackContent createFeedbackContent(Long idFeedback, String content, String type) {
        FeedbackContent feedbackContent = new FeedbackContent();
        feedbackContent.setType(type);
        feedbackContent.setComment(content);
        feedbackContent.setFeedbackId(idFeedback);
        feedbackContentRepository.save(feedbackContent);
        return feedbackContent;
    }

    @Override
    public FeedbackContentDTO findByFeedbackIdAndType(Long feedbackId, String type) {
        return feedbackContentMapper.toDto(feedbackContentRepository.findTopByFeedbackIdAndType(feedbackId, type));
    }

    @Override
    public List<FeedbackContentDTO> findListByFeedbackId(Long id){
        return feedbackContentMapper.toDto(feedbackContentRepository.findByFeedbackId(id));
    }
}
