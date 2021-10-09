package com.bkav.lk.service;

import com.bkav.lk.domain.Config;

import java.util.List;
import java.util.Map;

public interface ConfigService {

    Config findByPropertyCode(String code);

    Config createNewTermOfUseConfig();

    Config save(Config config);

    List<Config> findByPropertyGroup(String group);

    Boolean updateConfigCategory(Map<String, List<Object>> mapConfig);
}
