package com.bkav.lk.repository;

import com.bkav.lk.domain.CategoryConfigIcon;
import com.bkav.lk.repository.custom.CategoryConfigIconRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryConfigIconRepository extends JpaRepository<CategoryConfigIcon, Long>, CategoryConfigIconRepositoryCustom {
    List<CategoryConfigIcon> findByCodeMethodAndTypeAndHealthFacilitiesId(String codeMethod, String type, Long healthFacilitiesId);
}
