package com.bkav.lk.service.impl;

import com.bkav.lk.domain.ConfigIntegrated;
import com.bkav.lk.dto.ConfigIntegratedDTO;
import com.bkav.lk.repository.ConfigIntegratedRepository;
import com.bkav.lk.service.ConfigIntegratedService;
import com.bkav.lk.service.mapper.ConfigIntegratedMapper;
import com.bkav.lk.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConfigIntegratedServiceImpl implements ConfigIntegratedService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private static final String ENTITY_NAME = "ConfigIntegratedServiceImpl";

    private final ConfigIntegratedRepository configIntegratedRepository;

    private final ConfigIntegratedMapper configIntegratedMapper;

    @Value("${his.host}")
    private String HIS_HOST;

    @Value("${social-insurance.host}")
    private String SOCIAL_INSURANCE_HOST;

    public ConfigIntegratedServiceImpl(ConfigIntegratedRepository configIntegratedRepository, ConfigIntegratedMapper configIntegratedMapper) {
        this.configIntegratedRepository = configIntegratedRepository;
        this.configIntegratedMapper = configIntegratedMapper;
    }

    @Override
    public ConfigIntegratedDTO save(ConfigIntegratedDTO configIntegratedDTO) {
        log.debug("Request to save ConfigIntegrate : {}", configIntegratedDTO);

        // if don't have healthFacilityId => update social_insurance
        if (Objects.isNull(configIntegratedDTO.getHealthFacilityId())){
            configIntegratedDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
            ConfigIntegrated configIntegrated = configIntegratedRepository.save(configIntegratedMapper.toEntity(configIntegratedDTO));
            return configIntegratedMapper.toDto(configIntegrated);
        } else {
            List<ConfigIntegratedDTO> configIntegratedDTOS = this.findByHealthFacilityId(
                    configIntegratedDTO.getHealthFacilityId());
            // update or create config_integrated
            ConfigIntegratedDTO oldConfigIntegratedDTO = new ConfigIntegratedDTO();
            if (configIntegratedDTOS != null){
                oldConfigIntegratedDTO = configIntegratedDTOS
                        .stream()
                        .filter(item -> item.getConnectCode().equals("HIS"))
                        .findAny()
                        .orElse(null);
            }
            if (Objects.isNull(oldConfigIntegratedDTO)) {
                configIntegratedDTO.setStatus(Constants.ENTITY_STATUS.ACTIVE);
            } else {
                configIntegratedDTO.setId(oldConfigIntegratedDTO.getId());
                configIntegratedDTO.setStatus(oldConfigIntegratedDTO.getStatus());
            }
            configIntegratedDTO.setConnectCode(Constants.CONFIG_INTEGRATED.HIS_CONNECT_CODE);
            configIntegratedDTO.setConnectName(Constants.CONFIG_INTEGRATED.HIS_CONNECT_NAME);
            ConfigIntegrated configIntegrated = configIntegratedMapper.toEntity(configIntegratedDTO);
            configIntegrated = configIntegratedRepository.save(configIntegrated);
            return configIntegratedMapper.toDto(configIntegrated);
        }
    }

    @Override
    public ConfigIntegratedDTO findById(Long id) {
        return configIntegratedMapper.toDto(configIntegratedRepository.findById(id).orElse(null));
    }

    @Override
    public List<ConfigIntegratedDTO> findByHealthFacilityId(Long healthFacilityId) {
        log.info("Find ConfigIntegrated by healthFacilityId: {}", healthFacilityId);
        return configIntegratedMapper.toDto(configIntegratedRepository.findByHealthFacilityId(healthFacilityId));
    }

    @Override
    public ConfigIntegratedDTO findByConnectCodeAndHealthFacilityId(String connectCode, Long healthFacilityId) {
        log.info("Find ConfigIntegrated by healthFacilityId and connectCode: {}", healthFacilityId);
        return configIntegratedMapper
                .toDto(configIntegratedRepository.findByConnectCodeAndHealthFacilityIdAndStatus(connectCode, healthFacilityId, Constants.ENTITY_STATUS.ACTIVE).orElse(null));
    }

    @Override
    public List<ConfigIntegratedDTO> saveConfigIntegratedDefault(Long healthFacilityId) {
        // HIS
        ConfigIntegratedDTO configIntegratedDTOHIS = new ConfigIntegratedDTO();
        configIntegratedDTOHIS.setConnectName(Constants.CONFIG_INTEGRATED.HIS_CONNECT_NAME);
        configIntegratedDTOHIS.setConnectCode(Constants.CONFIG_INTEGRATED.HIS_CONNECT_CODE);
        configIntegratedDTOHIS.setConnectUrl(this.HIS_HOST);
        configIntegratedDTOHIS.setUsername(Constants.CONFIG_INTEGRATED.USERNAME_DEFAULT);
        configIntegratedDTOHIS.setPassword(Constants.CONFIG_INTEGRATED.PASSWORD_DEFAULT);
        configIntegratedDTOHIS.setStatus(Constants.ENTITY_STATUS.ACTIVE);
        configIntegratedDTOHIS.setHealthFacilityId(healthFacilityId);

        // Tất cả csyt dùng chung cấu hình thông tin bảo hiểm
        // SOCIAL_INSURANCE
//        ConfigIntegratedDTO configIntegratedSocialInsurance = new ConfigIntegratedDTO();
//        configIntegratedSocialInsurance.setConnectName(Constants.CONFIG_INTEGRATED.SOCIAL_INSURANCE_CONNECT_NAME);
//        configIntegratedSocialInsurance.setConnectCode(Constants.CONFIG_INTEGRATED.SOCIAL_INSURANCE_CONNECT_CODE);
//        configIntegratedSocialInsurance.setConnectUrl(this.SOCIAL_INSURANCE_HOST);
//        configIntegratedSocialInsurance.setUsername(Constants.CONFIG_INTEGRATED.USERNAME_DEFAULT);
//        configIntegratedSocialInsurance.setPassword(Constants.CONFIG_INTEGRATED.PASSWORD_DEFAULT);
//        configIntegratedSocialInsurance.setStatus(Constants.ENTITY_STATUS.ACTIVE);
//        configIntegratedSocialInsurance.setHealthFacilityId(healthFacilityId);

        List<ConfigIntegratedDTO> list = new ArrayList<>();
        list.add(configIntegratedDTOHIS);
//        list.add(configIntegratedSocialInsurance);
        list = configIntegratedMapper.toDto(configIntegratedRepository.saveAll(list.stream().map(configIntegratedMapper::toEntity).collect(Collectors.toList())));
        return list;
    }

    @Override
    public List<ConfigIntegratedDTO> findConfigIntegratedShared(String connectCode) {
        return configIntegratedMapper.toDto(configIntegratedRepository.findAllByConnectCodeAndStatus(connectCode, Constants.ENTITY_STATUS.ACTIVE));
    }
}
