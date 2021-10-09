package com.bkav.lk.service;

import com.bkav.lk.domain.Group;
import com.bkav.lk.dto.GroupDTO;
import com.bkav.lk.dto.GroupHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

public interface GroupService {

    GroupDTO save(GroupDTO groupDTO);

    Optional<GroupDTO> findByGroupName(String name);

    Page<GroupDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Optional<GroupDTO> findOne(Long id);

    void delete(Long id);

    List<String> getCreatedByUsers();

    List<GroupHistoryDTO> getGroupHistory(MultiValueMap<String, String> queryParam);

    Group findById(Long id);

}
