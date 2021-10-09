package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.PatientCard;
import com.bkav.lk.dto.PatientCardDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientCardMapper extends EntityMapper<PatientCardDTO, PatientCard>{
    PatientCard toEntity(PatientCardDTO patientCardDTO);

    PatientCardDTO toDto(PatientCard patientCard);

    default PatientCard fromId(Long id) {
        if(id == null) {
            return null;
        }
        PatientCard transaction = new PatientCard();
        transaction.setId(id);
        return transaction;
    }
}
