package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.MedicalResult;
import com.bkav.lk.dto.MedicalResultDTO;
import com.bkav.lk.web.rest.vm.ShortedMedicalResultVM;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
@DecoratedWith(MedicalResultDecorator.class)
public interface MedicalResultMapper extends EntityMapper<MedicalResultDTO, MedicalResult> {

    @Mapping(target = "patient.id", source = "medicalResultDTO.patientId")
    MedicalResult toEntity(MedicalResultDTO medicalResultDTO);

    MedicalResultDTO toDto(MedicalResult medicalResult);

    default MedicalResult fromId(Long id) {
        if (id == null) {
            return null;
        }
        MedicalResult medicalResult = new MedicalResult();
        medicalResult.setId(id);
        return medicalResult;
    }

    ShortedMedicalResultVM toVM(MedicalResultDTO medicalResultDTO);

    List<ShortedMedicalResultVM> toVM(List<MedicalResultDTO> medicalResultDTO);

    MedicalResultDTO toDto(ShortedMedicalResultVM shortedMedicalResultVM);

    List<MedicalResultDTO> toCollectionDto(List<ShortedMedicalResultVM> medicalResultVMs);
}
