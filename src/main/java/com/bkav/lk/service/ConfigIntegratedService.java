package com.bkav.lk.service;

import com.bkav.lk.dto.ConfigIntegratedDTO;

import java.util.List;

public interface ConfigIntegratedService {

    ConfigIntegratedDTO save(ConfigIntegratedDTO configIntegratedDTO);

    ConfigIntegratedDTO findById(Long id);

    ConfigIntegratedDTO findByConnectCodeAndHealthFacilityId(String connectCode, Long healthFacilityId);

    List<ConfigIntegratedDTO> findByHealthFacilityId(Long healthFacilityId);

    List<ConfigIntegratedDTO> saveConfigIntegratedDefault(Long healthFacilityId);

    List<ConfigIntegratedDTO> findConfigIntegratedShared(String connectCode);


}
