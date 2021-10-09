package com.bkav.lk.repository;


import com.bkav.lk.domain.CategoryConfigFieldMain;
import com.bkav.lk.repository.custom.CategoryConfigFieldMainRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryConfigFieldMainRepository extends JpaRepository<CategoryConfigFieldMain, Long>, CategoryConfigFieldMainRepositoryCustom {

    List<CategoryConfigFieldMain> findByColumnNameAndTypeAndHealthFacilitiesId(String columnName, String type, Long healthFacilitiesId);
}
