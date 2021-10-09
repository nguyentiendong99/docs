package com.bkav.lk.repository.custom;

import com.bkav.lk.domain.CategoryConfigValue;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * @author hieu.daominh
 * <p>
 * The interface Category config value repository custom.
 */
public interface CategoryConfigValueRepositoryCustom {

    /**
     * Search list.
     *
     * @param queryParams the query params
     * @param pageable    the pageable
     * @return the list
     */
    List<CategoryConfigValue> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    /**
     * Count total categoryConfigValue.
     *
     * @param queryParams the query params
     * @return the long
     */
    Long count(MultiValueMap<String, String> queryParams);

}
