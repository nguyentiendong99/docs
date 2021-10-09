package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.Area;

import java.util.List;

public interface AreaRepositoryCustom {
    List<Area> findAllByParentCode(String parentCode);
}
