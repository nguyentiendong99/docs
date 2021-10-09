package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.Position;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

public interface PositionRepositoryCustom {

    List<Position> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Long count(MultiValueMap<String, String> queryParams);

    List<Position> findAllChildrenByParentId(Long parentId);

}
