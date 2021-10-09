package com.bkav.lk.service;

import com.bkav.lk.dto.TopicCustomConfigDTO;
import com.bkav.lk.dto.TopicDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface TopicService {

    Optional<TopicDTO> findOne(Long id);

    Page<TopicDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    TopicDTO save(TopicDTO topicDTO);

    void delete(String ids);

    List<TopicDTO> findByStatus(Integer status);

    ByteArrayInputStream exportTopicToExcel(List<TopicDTO> list, InputStream file);

    boolean isDeactivable(Long id);

    /**
     * Find all custom config by topic id list.
     *
     * @param topicId the topic id
     * @return the list
     */
    List<TopicCustomConfigDTO> findAllCustomConfigByTopicId(Long topicId, Long healthFacilityId);
}
