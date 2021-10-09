package com.bkav.lk.repository;

import com.bkav.lk.domain.Area;
import com.bkav.lk.repository.custom.AreaRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long>, AreaRepositoryCustom {
    Area findByAreaCode(String areaCode);

    @Query("SElECT A FROM Area A WHERE A.level = :level AND A.status = :status")
    List<Area> findByLevelAndStatus(Integer level, Integer status);

    List<Area> findByStatusAndParentCodeAndNameLike(Integer status, String parentCode, String name);
}
