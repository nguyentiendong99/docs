package com.bkav.lk.service;

import com.bkav.lk.dto.AreaDTO;

import java.util.List;

public interface AreaService {
    List<AreaDTO> findByParentCode(String parentCode);

    List<AreaDTO> findByLevelAndStatus(Integer level, Integer status);

    List<AreaDTO> findByNameAndParentCodeAndStatus(String name, String parentCode, Integer status);
}
