package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.CategoryConfigField;
import com.bkav.lk.repository.custom.CategoryConfigFieldRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class CategoryConfigFieldRepositoryImpl implements CategoryConfigFieldRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CategoryConfigField> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        return null;
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        return null;
    }
}
