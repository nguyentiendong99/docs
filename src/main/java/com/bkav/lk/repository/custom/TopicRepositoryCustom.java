package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.Topic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface TopicRepositoryCustom {

    List<Topic> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    void delete(String ids);

    List<Topic> findByStatus(Integer status);
}
