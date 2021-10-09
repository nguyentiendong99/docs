package com.bkav.lk.repository;

import com.bkav.lk.domain.Feedback;
import com.bkav.lk.repository.custom.FeedbackRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, FeedbackRepositoryCustom {

    List<Feedback> findAllByUserId(Long userId);

    @Modifying
    @Query("update Feedback f set f.status =:statusDone where f.lastModifiedDate < :date and f.status = :status")
    @Transactional
    void ChangeStatus(Instant date, Integer status, Integer statusDone);

    List<Feedback> findByTopicId(Long topicId);
}
