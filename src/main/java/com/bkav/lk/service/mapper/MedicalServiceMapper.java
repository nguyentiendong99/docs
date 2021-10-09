package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.MedicalService;
import com.bkav.lk.dto.MedicalServiceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MedicalServiceMapper extends EntityMapper<MedicalServiceDTO, MedicalService> {

    @Mappings({
            @Mapping(target = "healthFacilities.id", source = "medicalServiceDTO.healthFacilityId")
    })
    MedicalService toEntity(MedicalServiceDTO medicalServiceDTO);

    @Mappings({
            @Mapping(target = "healthFacilityId", source = "medicalService.healthFacilities.id")
    })
    MedicalServiceDTO toDto(MedicalService medicalService);

    default MedicalService fromId(Long id){
        if(id == null){
            return null;
        }
        MedicalService medicalService = new MedicalService();
        medicalService.setId(id);
        return medicalService;
    }

}
