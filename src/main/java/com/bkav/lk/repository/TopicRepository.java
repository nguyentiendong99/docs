package com.bkav.lk.repository;

import com.bkav.lk.domain.Topic;
import com.bkav.lk.repository.custom.TopicRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long>, TopicRepositoryCustom {
    Topic findTopByCode(String code);
}

