package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.MedicalSpeciality;
import com.bkav.lk.dto.MedicalSpecialityDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MedicalSpecialityMapper extends EntityMapper<MedicalSpecialityDTO, MedicalSpeciality> {

    @Mappings({
            @Mapping(target = "healthFacilities.id", source = "medicalSpecialityDTO.healthFacilityId"),
            @Mapping(target = "healthFacilities.code", source = "medicalSpecialityDTO.healthFacilityCode"),
            @Mapping(target = "healthFacilities.name", source = "medicalSpecialityDTO.healthFacilityName")
    })
    MedicalSpeciality toEntity(MedicalSpecialityDTO medicalSpecialityDTO);

    @Mappings({
            @Mapping(target = "healthFacilityId", source = "medicalSpeciality.healthFacilities.id"),
            @Mapping(target = "healthFacilityCode", source = "medicalSpeciality.healthFacilities.code"),
            @Mapping(target = "healthFacilityName", source = "medicalSpeciality.healthFacilities.name")
    })
    MedicalSpecialityDTO toDto(MedicalSpeciality medicalSpeciality);

    default MedicalSpeciality fromId(Long id) {
        if (id == null)
            return null;
        MedicalSpeciality medicalSpeciality = new MedicalSpeciality();
        medicalSpeciality.setId(id);
        return medicalSpeciality;
    }
}
