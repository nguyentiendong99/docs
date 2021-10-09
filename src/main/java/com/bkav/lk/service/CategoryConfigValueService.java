package com.bkav.lk.service;

import com.bkav.lk.dto.CategoryConfigValueDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * The interface Category config value service.
 *
 * @author hieu.daominh  The interface Category config field service.
 */
public interface CategoryConfigValueService {

    /**
     * Search page.
     *
     * @param queryParams the query params
     * @param pageable    the pageable
     * @return the page
     */
    Page<CategoryConfigValueDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    /**
     * Find by id category config field dto.
     *
     * @param id the id
     * @return the category config field dto
     * @throws EntityNotFoundException the entity not found exception
     */
    CategoryConfigValueDTO findById(Long id) throws EntityNotFoundException;

    /**
     * Find by id and status category config field dto.
     *
     * @param fieldId the field id
     * @param status  the status
     * @return the category config field dto
     */
    CategoryConfigValueDTO findByIdAndStatus(Long fieldId, Integer status);

    /**
     * Find all list.
     *
     * @return the list
     */
    List<CategoryConfigValueDTO> findAll();

    /**
     * Create category config field dto.
     *
     * @param categoryConfigFieldDTO the category config field dto
     * @return the category config field dto
     */
    CategoryConfigValueDTO create(CategoryConfigValueDTO categoryConfigFieldDTO);

    /**
     * Update category config field dto.
     *
     * @param categoryConfigFieldDTO the category config field dto
     * @return the category config field dto
     * @throws EntityNotFoundException the entity not found exception
     */
    CategoryConfigValueDTO update(CategoryConfigValueDTO categoryConfigFieldDTO) throws EntityNotFoundException;

    /**
     * Update all category config value dto.
     *
     * @param categoryConfigFieldDTOS the category config field dtos
     * @return the category config value dto
     * @throws EntityNotFoundException the entity not found exception
     */
    List<CategoryConfigValueDTO> updateAll(List<CategoryConfigValueDTO> categoryConfigFieldDTOS);

    /**
     * Create all list.
     *
     * @param categoryConfigFieldDTOS the category config field dtos
     * @return the list
     */
    List<CategoryConfigValueDTO> createAll(List<CategoryConfigValueDTO> categoryConfigFieldDTOS);

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
     * Find all by field id and status list.
     *
     * @param fieldId the field id
     * @param status  the status
     * @return List<CategoryConfigValueDTO> list
     */
    List<CategoryConfigValueDTO> findAllByFieldIdAndStatus(Long fieldId, Integer status);

    /**
     * Find all by field id list.
     *
     * @param fieldId the field id
     * @return the list
     */
    List<CategoryConfigValueDTO> findAllByFieldId(Long fieldId);

    /**
     * Find all by object id list.
     *
     * @param objectId the object id
     * @return the list
     */
    List<CategoryConfigValueDTO> findAllByObjectId(Long objectId);

    /**
     * Find by object id and field id list.
     *
     * @param objectId the object id
     * @param fieldId  the field id
     * @return the list
     */
    CategoryConfigValueDTO findByObjectIdAndFieldId(Long objectId, Long fieldId);
}
