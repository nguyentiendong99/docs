package com.bkav.lk.repository;

import com.bkav.lk.domain.Config;
import com.bkav.lk.repository.custom.ConfigRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigRepository extends JpaRepository<Config, Long>, ConfigRepositoryCustom {

    Config findByPropertyCode(String code);

    List<Config> findByPropertyGroup(String propertyGroup);
}
