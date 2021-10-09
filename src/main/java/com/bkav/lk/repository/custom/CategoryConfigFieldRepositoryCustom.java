package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.CategoryConfigField;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * @author hieu.daominh
 *
 * The interface Category config field repository custom.
 */
public interface CategoryConfigFieldRepositoryCustom {

    /**
     * Search list.
     *
     * @param queryParams the query params
     * @param pageable    the pageable
     * @return the list CategoryConfigField
     */
    List<CategoryConfigField> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    /**
     * Count total categoryConfigField.
     *
     * @param queryParams the query params
     * @return the long
     */
    Long count(MultiValueMap<String, String> queryParams);

}
