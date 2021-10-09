package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.ConfigIntegrated;
import com.bkav.lk.dto.ConfigIntegratedDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigIntegratedMapper extends EntityMapper<ConfigIntegratedDTO, ConfigIntegrated> {
    ConfigIntegrated toEntity(ConfigIntegratedDTO configIntegratedDTO);

    ConfigIntegratedDTO toDto(ConfigIntegrated configIntegrated);

    default ConfigIntegrated fromId(Long id) {
        if(id == null) {
            return null;
        }
        ConfigIntegrated configIntegrated = new ConfigIntegrated();
        configIntegrated.setId(id);
        return configIntegrated;
    }
}
