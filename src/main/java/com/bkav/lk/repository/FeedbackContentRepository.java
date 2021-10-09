package com.bkav.lk.repository;


import com.bkav.lk.domain.FeedbackContent;
import com.bkav.lk.repository.custom.FeedbackContentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackContentRepository extends JpaRepository<FeedbackContent, Long>, FeedbackContentRepositoryCustom {
    FeedbackContent findTopByFeedbackIdAndType(Long feedbackId,String type);

    List<FeedbackContent> findByFeedbackId(Long id);
}
