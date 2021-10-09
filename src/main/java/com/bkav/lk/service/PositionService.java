package com.bkav.lk.service;

import com.bkav.lk.domain.Position;
import com.bkav.lk.dto.PositionDTO;
import com.bkav.lk.dto.PositionHistoryDTO;
import com.bkav.lk.web.errors.ErrorExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PositionService {

    Page<PositionDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Optional<PositionDTO> findOne(Long id);

    Position findById(Long id);

    PositionDTO save(PositionDTO positionDTO);

    void delete(Long id);

    boolean existCode(String code);

    Map<String, Object> handleTreePosition(List<PositionDTO> list, Map<String, String> params);

    List<PositionDTO> findAll();

    List<PositionDTO> findAllChildrenByParentId(Long parentId);

    List<PositionDTO> excelToPositions(InputStream inputStream, List<ErrorExcel> details);

    String generatePositionCode(String code, String name, boolean checkName);

    List<PositionHistoryDTO> getPositionHistory(MultiValueMap<String, String> queryParam);

    List<PositionDTO> findAllActiveStatus();
}
