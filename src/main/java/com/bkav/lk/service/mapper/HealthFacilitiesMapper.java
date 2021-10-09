package com.bkav.lk.service.mapper;


import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.dto.HealthFacilitiesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthFacilitiesMapper extends EntityMapper<HealthFacilitiesDTO , HealthFacilities> {
    HealthFacilities toEntity(HealthFacilitiesDTO healthFacilitiesDTO);

    HealthFacilitiesDTO toDto(HealthFacilities healthFacilities);

    default HealthFacilities fromId(Long id) {
        if(id == null) {
            return null;
        }
        HealthFacilities healthFacilities = new HealthFacilities();
        healthFacilities.setId(id);
        return healthFacilities;
    }

}
