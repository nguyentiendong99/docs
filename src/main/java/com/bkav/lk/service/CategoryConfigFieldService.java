package com.bkav.lk.service;

import com.bkav.lk.dto.CategoryConfigFieldDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * The interface Category config field service.
 *
 * @author hieu.daominh The interface Category config field service.
 */
public interface CategoryConfigFieldService {

    /**
     * Search page.
     *
     * @param queryParams the query params
     * @param pageable    the pageable
     * @return the page
     */
    Page<CategoryConfigFieldDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    /**
     * Find by id category config field dto.
     *
     * @param id the id
     * @return the category config field dto
     * @throws EntityNotFoundException the entity not found exception
     */
    CategoryConfigFieldDTO findById(Long id) throws EntityNotFoundException;

    /**
     * Find by id and status category config field dto.
     *
     * @param fieldId the field id
     * @param status  the status
     * @return the category config field dto
     */
    CategoryConfigFieldDTO findByIdAndStatus(Long fieldId, Integer status);

    /**
     * Find all list.
     *
     * @return the list
     */
    List<CategoryConfigFieldDTO> findAll();

    /**
     * Find all list by status.
     *
     * @param status the status
     * @return the list
     */
    List<CategoryConfigFieldDTO> findAllByStatus(Integer status);


    /**
     * Create list.
     *
     * @param categoryConfigFieldDTOs the category config field dt os
     * @return the list
     */
    List<CategoryConfigFieldDTO> create(List<CategoryConfigFieldDTO> categoryConfigFieldDTOs);


    /**
     * Update list.
     *
     * @param categoryConfigFieldDTOs the category config field dt os
     * @return the list
     * @throws EntityNotFoundException the entity not found exception
     */
    List<CategoryConfigFieldDTO> update(List<CategoryConfigFieldDTO> categoryConfigFieldDTOs) throws EntityNotFoundException;

    /**
     * Check exits by id.
     *
     * @param id     the id
     * @param status the status
     * @return the boolean
     */
    boolean checkExitsByIdAndStatusNot(Long id, Integer status);

    /**
     * Check exits by id boolean.
     *
     * @param id the id
     * @return the boolean
     */
    boolean checkExitsById(Long id);

    /**
     * Delete.
     *
     * @param id the id
     * @throws EntityNotFoundException the entity not found exception
     */
    void delete(Long id) throws EntityNotFoundException;

    /**
     * Delete all.
     *
     * @param ids the ids
     */
    void deleteAll(List<Long> ids);


    /**
     * Find all by health facilities id and status.
     *
     * @param healthFacilityId the health facility id
     * @param status           the status
     * @param configType       the config type
     * @return the list
     */
    List<CategoryConfigFieldDTO> findAllByHealthFacilityIdAndStatusAndConfigType(Long healthFacilityId, Integer status, Integer configType);

    /**
     * Find all topic by health facilities id and status.
     *
     * @param healthFacilityId the health facility id
     * @param status           the status
     * @param configType      the config type
     * @return the list
     */
    List<CategoryConfigFieldDTO> findAllReportByHealthFacilityIdAndStatusAndConfigType(Long healthFacilityId, Integer status, Integer configType);

    List<CategoryConfigFieldDTO> findAllPatientByHealthFacilityIdAndStatusAndConfigType(Long healthFacilityId, Integer status, Integer configType);


    /**
     * Find all by health facilities id and status.
     *
     * @param healthFacilityId the health facility id
     * @param status           the status
     * @return the list
     */
    List<CategoryConfigFieldDTO> findAllByHealthFacilityIdAndStatus(Long healthFacilityId, Integer status);

    List<CategoryConfigFieldDTO> findAllByHealthFacilityIdAndConfigTypeAndStatusIn(Long healthFacilityId, Integer configType, Integer[] status);

    List<CategoryConfigFieldDTO> findAllReportByHealthFacilityIdAndConfigTypeAndStatusIn(Long healthFacilityId, Integer configType, Integer[] status);

    List<CategoryConfigFieldDTO> findAllPatientByHealthFacilityIdAndConfigTypeAndStatusIn(Long healthFacilityId, Integer configType, Integer[] status);

}
