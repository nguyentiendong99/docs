package com.bkav.lk.repository;

import com.bkav.lk.domain.CategoryConfigValue;
import com.bkav.lk.repository.custom.CategoryConfigValueRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author hieu.daominh
 *
 * The interface Category config value repository.
 */
@Repository
public interface CategoryConfigValueRepository extends JpaRepository<CategoryConfigValue, Long>, CategoryConfigValueRepositoryCustom {

    /**
     * Find by id and status.
     *
     * @param fieldId the field id
     * @param status the status of value-field
     * @return the Optional<CategoryConfigValue>
     */
    Optional<CategoryConfigValue> findByIdAndStatus(Long fieldId, Integer status);

    /**
     * Find all by field id and status.
     *
     * @param fieldId the field id
     * @param status  the status
     * @return the Optional<List<CategoryConfigValue>>
     */
    Optional<List<CategoryConfigValue>> findAllByFieldIdAndStatus(Long fieldId, Integer status);

    /**
     * Find all by field id optional.
     *
     * @param fieldId the field id
     * @return the optional
     */
    Optional<List<CategoryConfigValue>> findAllByFieldId(Long fieldId);

    /**
     * Find all by object id and status optional.
     *
     * @param objectId the object id
     * @param status   the status
     * @return the optional
     */
    Optional<List<CategoryConfigValue>> findAllByObjectIdAndStatus(Long objectId, Integer status);

    /**
     * Find by object id and field id and status optional.
     *
     * @param objectId the object id
     * @param fieldId  the field id
     * @param status   the status
     * @return the optional
     */
    Optional<CategoryConfigValue> findByObjectIdAndFieldIdAndStatus(Long objectId, Long fieldId, Integer status);
}
