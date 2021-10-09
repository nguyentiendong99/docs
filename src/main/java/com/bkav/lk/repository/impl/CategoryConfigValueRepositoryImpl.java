package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.CategoryConfigValue;
import com.bkav.lk.repository.custom.CategoryConfigValueRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class CategoryConfigValueRepositoryImpl implements CategoryConfigValueRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CategoryConfigValue> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        return null;
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        return null;
    }
}
