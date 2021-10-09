package com.bkav.lk.service;

import com.bkav.lk.domain.User;
import com.bkav.lk.dto.HealthFacilitiesDTO;
import com.bkav.lk.dto.HealthFacilitiesHistoryDTO;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.web.errors.ErrorExcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HealthFacilitiesService {
    Optional<HealthFacilitiesDTO> findOne(Long id);

    Page<HealthFacilitiesDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    HealthFacilitiesDTO save(HealthFacilitiesDTO healthFacilitiesDTO);

    List<HealthFacilitiesDTO> findByStatusActiveOrUnActive();

    Map<String, Object> handleListToTree(List<HealthFacilitiesDTO> list ,Map<String, String> params);

    void delete(Long id);

    List<HealthFacilitiesDTO> findAllChildrenByParentId(Long parent);

    List<HealthFacilitiesDTO> findByStatus(List<Integer> status);

    HealthFacilitiesDTO findById(Long id);

    Boolean checkExistCode(String code);

    List<HealthFacilitiesDTO> findAllHealthFacilities(Integer appointmentOption);

    List<HealthFacilitiesDTO> findAllHealthFacilitiesByUser(Integer appointmentOption, User user);

    List<HealthFacilitiesDTO> findByParentId(Long parentId);

    List<HealthFacilitiesHistoryDTO> getHealthFacilitiesHistoryById(Long id);

    List<HealthFacilitiesDTO> getHealthFacilitiesParentAndThis(Long id);

    List<HealthFacilitiesDTO> excelToHealthFacilities(InputStream inputStream, List<ErrorExcel> details);

    ResultExcel bulkUploadHealthFacilities(List<HealthFacilitiesDTO> list);

    List<HealthFacilitiesDTO> findAllByParentAndStatusGreaterThan(Long parentId, Integer status);

    /**
     * Check exits by id and status boolean.
     *
     * @param id     the id
     * @param status the status
     * @return the boolean
     */
    boolean checkExitsByIdAndStatus(Long id, Integer status);

    HealthFacilitiesDTO findByCodeAndStatusGreaterThan(String code, Integer status);
}
