package com.bkav.lk.service.mapper;

//<editor-fold desc="IMPORT">

import com.bkav.lk.domain.Clinic;
import com.bkav.lk.domain.MedicalSpeciality;
import com.bkav.lk.dto.ClinicDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
//</editor-fold>

@Mapper(componentModel = "spring")
public interface ClinicMapper extends EntityMapper<ClinicDTO, Clinic> {

    @Mapping(target = "healthFacilities.id", source = "clinicDTO.healthFacilityId")
    Clinic toEntity(ClinicDTO clinicDTO);

    @Mapping(target = "healthFacilityId", source = "clinic.healthFacilities.id")
    ClinicDTO toDto(Clinic clinic);

    default Clinic fromId(Long id){
        if(id == null){
            return null;
        }
        Clinic clinic = new Clinic();
        clinic.setId(id);
        return clinic;
    }
}
